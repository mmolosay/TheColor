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
    fun `when 'set color' command is sent, then it only completes after the color has been processed and 'dataFlow' emitted a new value`() =
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
                val command = ColorPreviewCommand.SetColor(color = color)
                sut.commands.send(command)
                command.completion.await() // will suspend indefinitely until gate is open
            }
            emittedData.shouldBeEmpty() // no new data has been emitted yet

            gate.open() // will make 'dataFlow' process new color and emit new value
            emittedData.shouldNotBeEmpty()
            dataCollectionJob.cancel()
        }

    /**
     * Tests that all ongoing [ColorPreviewViewModel.process] calls for [ColorPreviewCommand.SetColor]
     * are canceled when the new [ColorPreviewCommand.SetColor] command is sent.
     */
    @Test
    fun `given that 'set color' command is being processed, when another 'set color' command is sent, then processing of the first command is canceled`() =
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
            val sendCommand1Job = launch {
                val command = ColorPreviewCommand.SetColor(color1)
                sut.commands.send(command)
                command.completion.await()
                error("this CompletableDeferred should never complete")
            }
            emittedData.shouldBeEmpty()
            launch {
                val command = ColorPreviewCommand.SetColor(color2)
                sut.commands.send(command)
                command.completion.await()
                println() // TODO: remove
            }
            emittedData.shouldBeEmpty()

            gate.open()
            emittedData.size shouldBe 1 // processing of 'color1' should've been canceled and thus no data emitted

            dataCollectionJob.cancel()
            sendCommand1Job.cancel()
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