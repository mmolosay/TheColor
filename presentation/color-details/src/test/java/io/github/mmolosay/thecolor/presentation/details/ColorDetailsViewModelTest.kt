package io.github.mmolosay.thecolor.presentation.details

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.usecase.GetColorDetailsUseCase
import io.github.mmolosay.thecolor.presentation.ColorCenterCommandProvider
import io.github.mmolosay.thecolor.presentation.ColorCenterCommand
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsViewModel.State
import io.github.mmolosay.thecolor.testing.MainDispatcherRule
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beOfType
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ColorDetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    val commandProvider: ColorCenterCommandProvider = mockk()
    val getColorDetails: GetColorDetailsUseCase = mockk()
    val createData: CreateColorDetailsDataUseCase = mockk()

    lateinit var sut: ColorDetailsViewModel

    @Test
    fun `SUT remains dormant if there's no 'fetch data' command emitted`() {
        every { commandProvider.commandFlow } returns emptyFlow()

        createSut()

        sut.dataStateFlow.value shouldBe State.Loading
    }

    @Test
    fun `emission of 'fetch data' command results in emission of Ready state`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val color = Color.Hex(0x1A803F)
            val commandFlow = MutableSharedFlow<ColorCenterCommand>()
            every { commandProvider.commandFlow } returns commandFlow
            coEvery { getColorDetails.invoke(any<Color>()) } returns mockk()
            every { createData(details = any()) } returns mockk()
            createSut()

            commandFlow.emit(ColorCenterCommand.FetchData(color))

            sut.dataStateFlow.value should beOfType<State.Ready>()
        }

    fun createSut() =
        ColorDetailsViewModel(
            commandProvider = commandProvider,
            getColorDetails = getColorDetails,
            createData = createData,
            ioDispatcher = mainDispatcherRule.testDispatcher,
        ).also {
            sut = it
        }
}