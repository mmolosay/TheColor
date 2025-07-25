package io.github.mmolosay.thecolor.presentation.preview

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.usecase.ColorConverter
import io.github.mmolosay.thecolor.presentation.api.ColorToColorIntUseCase
import io.github.mmolosay.thecolor.testing.MainDispatcherExtension
import io.github.mmolosay.thecolor.utils.ClosableSuspendGate
import io.github.mmolosay.thecolor.utils.OpenSuspendGate
import io.github.mmolosay.thecolor.utils.SuspendGate
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MainDispatcherExtension::class)
class ColorPreviewViewModelTest {

    val testDispatcher = UnconfinedTestDispatcher()
    val colorFlow = MutableSharedFlow<Color?>()
    val colorProcessedConfirmationChannelReal = Channel<Color?>(Channel.UNLIMITED)
    val colorProcessedConfirmationChannelMock: Channel<Color?> = mockk(relaxed = true)

    lateinit var sut: ColorPreviewViewModel

    @Test
    fun `when 'colorFlow' emits new color, then confirmation is sent only after 'dataFlow' emits new value`() =
        runTest(testDispatcher) {
            val gate = ClosableSuspendGate(closed = true)
            createSut(
                colorProcessedConfirmationChannel = colorProcessedConfirmationChannelMock,
                gateForDataFlow = gate,
            )

            val color = Color.Hex(0x0)
            colorFlow.emit(color)
            coVerify(exactly = 0) {
                colorProcessedConfirmationChannelMock.send(color)
            }

            gate.open() // will make 'dataFlow' process new color and emit new value
            coVerify(exactly = 1) {
                colorProcessedConfirmationChannelMock.send(color)
            }
        }

    fun createSut(
        colorProcessedConfirmationChannel: Channel<Color?> = colorProcessedConfirmationChannelReal,
        gateForDataFlow: SuspendGate = OpenSuspendGate,
    ): ColorPreviewViewModel =
        ColorPreviewViewModel(
            coroutineScope = CoroutineScope(SupervisorJob() + testDispatcher),
            colorFlow = colorFlow,
            colorProcessedConfirmationChannel = colorProcessedConfirmationChannel,
            gateForDataFlow = gateForDataFlow,
            colorToColorInt = ColorToColorIntUseCase(ColorConverter()), // no need to use mock
            defaultDispatcher = testDispatcher,
        ).also {
            sut = it
        }
}