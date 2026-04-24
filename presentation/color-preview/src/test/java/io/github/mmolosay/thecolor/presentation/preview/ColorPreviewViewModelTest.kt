package io.github.mmolosay.thecolor.presentation.preview

import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorConverter
import io.github.mmolosay.thecolor.presentation.common.colorint.ColorToColorIntUseCase
import io.github.mmolosay.thecolor.testing.MainDispatcherExtension
import io.github.mmolosay.thecolor.utils.ClosableSuspendGate
import io.github.mmolosay.thecolor.utils.OpenSuspendGate
import io.github.mmolosay.thecolor.utils.SuspendGate
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
class ColorPreviewViewModelTest {

    val testDispatcher = UnconfinedTestDispatcher()

    @RegisterExtension
    @Suppress("unused")
    val mainDispatcherExtension = MainDispatcherExtension(testDispatcher)

    lateinit var sut: ColorPreviewViewModel

    @Test
    fun `when 'set color' is invoked, then the job completes only after the color has been processed and 'dataFlow' emitted a new value`() =
        runTest(testDispatcher) {
            val gate = ClosableSuspendGate(closed = true)
            createSut(
                gateForDataFlow = gate,
            )

            val emittedData = mutableListOf<ColorPreviewData?>()
            val dataCollectionJob = launch {
                sut.dataFlow
                    .drop(1) // replayed value of 'StateFlow'
                    .toList(emittedData)
            }
            val color = Color.Hex(0x0)
            launch {
                sut.setColor(color).join() // will suspend indefinitely until gate is open
            }
            emittedData.shouldBeEmpty() // no new data has been emitted yet

            gate.open() // will make 'dataFlow' process new color and emit new value
            emittedData.shouldNotBeEmpty()
            dataCollectionJob.cancel()
        }

    /**
     * Tests that all ongoing [ColorPreviewViewModel.setColor] calls are canceled
     * when the new [ColorPreviewViewModel.setColor] call is made.
     */
    @Test
    fun `given there is an ongoing 'set color' job, when it is called again, then the ongoing job is cancelled`() =
        runTest(testDispatcher) {
            val gate = ClosableSuspendGate(closed = true)
            createSut(
                gateForDataFlow = gate,
            )

            val emittedData = mutableListOf<ColorPreviewData?>()
            val dataCollectionJob = launch {
                sut.dataFlow
                    .drop(1) // replayed value of 'StateFlow'
                    .toList(emittedData)
            }
            val color1 = Color.Hex(0x0)
            val color2 = Color.Hex(0x1)
            launch {
                sut.setColor(color1).join()
            }
            emittedData.shouldBeEmpty()
            launch {
                sut.setColor(color2).join()
            }
            emittedData.shouldBeEmpty()

            gate.open() // open gate to allow first 'setColor()' to complete if it's not canceled
            emittedData.size shouldBe 1 // processing of 'color1' should've been canceled and thus no data emitted

            dataCollectionJob.cancel()
        }

    fun createSut(
        gateForDataFlow: SuspendGate = OpenSuspendGate,
    ): ColorPreviewViewModel =
        ColorPreviewViewModel(
            coroutineScope = CoroutineScope(SupervisorJob() + testDispatcher),
            gateForDataFlow = gateForDataFlow,
            colorToColorInt = ColorToColorIntUseCase(ColorConverter()), // no need to use mock
            defaultDispatcher = testDispatcher,
        ).also {
            sut = it
        }
}