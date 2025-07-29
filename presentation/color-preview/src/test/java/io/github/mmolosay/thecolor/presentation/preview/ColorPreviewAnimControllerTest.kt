package io.github.mmolosay.thecolor.presentation.preview

import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewAnimController.VisibilityWithCause
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.junit.jupiter.api.Test
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewAnimState as AnimState
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewUiState as UiState

class ColorPreviewAnimControllerTest {

    lateinit var sut: ColorPreviewAnimController

    @Test
    fun `when 'UiState Visible' arrives, then visibility dest becomes 'Expanded'`() {
        sut = ColorPreviewAnimControllerImpl(uiState = UiState.Hidden)

        val newUiState = mockk<UiState.Visible>()
        sut.onNewUiState(newUiState)

        sut.flowOfVisibilityDest.value shouldBe VisibilityWithCause(
            value = AnimState.Visibility.Expanded,
            cause = newUiState,
        )
    }

    /**
     * Given [AnimState.Visibility.Collapsed] is being animated,
     * when new [UiState.Visible] arrives,
     * then visibility dest becomes [AnimState.Visibility.Expanded].
     */
    @Test
    fun `given 'Collapsed' visibility is being animated, when new 'UiState Visible' arrives, then visibility dest becomes 'Expanded'`() {
        sut = ColorPreviewAnimControllerImpl(uiState = mockk<UiState.Visible>())
        sut.onNewUiState(uiState = UiState.Hidden) // starts collapsing

        val newUiState = mockk<UiState.Visible>()
        sut.onNewUiState(newUiState)

        sut.flowOfVisibilityDest.value shouldBe VisibilityWithCause(
            value = AnimState.Visibility.Expanded,
            cause = newUiState,
        )
    }

    /**
     * Given [sut] is created with [UiState.Hidden],
     * when new [UiState.Visible] X arrives,
     * then [ColorPreviewAnimController.mainUiState] becomes X,
     * so that it's visible during expanding animation.
     */
    @Test
    fun `given SUT is created with 'UiState Hidden', when new 'UiState Visible' arrives, then 'mainUiState' is updated`() {
        sut = ColorPreviewAnimControllerImpl(uiState = UiState.Hidden)

        val newUiState = mockk<UiState.Visible>()
        sut.onNewUiState(uiState = newUiState)

        sut.mainUiState shouldBe newUiState
    }
}