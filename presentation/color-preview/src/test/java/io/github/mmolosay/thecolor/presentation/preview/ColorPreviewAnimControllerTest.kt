package io.github.mmolosay.thecolor.presentation.preview

import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewAnimController.VisibilityWithCause
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewAnimState.Visibility
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewUiState as UiState

@OptIn(ExperimentalCoroutinesApi::class)
class ColorPreviewAnimControllerTest {

    val testDispatcher = UnconfinedTestDispatcher()

    lateinit var sut: ColorPreviewAnimController

    @Test
    fun `given initial 'UiState Hidden', when new 'UiState Visible' arrives, then visibility dest becomes 'Expanded'`() {
        sut = ColorPreviewAnimControllerImpl(uiState = mockk<UiState.Hidden>())

        val newUiState = mockk<UiState.Visible>()
        sut.onNewUiState(newUiState)

        sut.visibilityDest shouldBe VisibilityWithCause(
            value = Visibility.Expanded,
            cause = newUiState,
        )
    }

    @Test // ANCHOR:Label=0
    fun `given initial 'UiState Visible', when new 'UiState Visible' arrives, then an update of visible UI state is added`() {
        sut = ColorPreviewAnimControllerImpl(uiState = mockk<UiState.Visible>())
        sut.updatesOfVisibleUiState shouldHaveSize 0

        val newUiState = mockk<UiState.Visible>()
        sut.onNewUiState(newUiState)

        sut.updatesOfVisibleUiState shouldHaveSize 1
    }

    @Test
    fun `given main anim 'Collapsed → Visible' is running, when new 'UiState Hidden' arrives, then visibility dest becomes 'Collapsed'`() {
        sut = ColorPreviewAnimControllerImpl(uiState = mockk<UiState.Hidden>())

        val visibleUiState = mockk<UiState.Visible>()
        sut.onNewUiState(visibleUiState)
        sut.onMainVisibilityAnimStarted(dest = sut.visibilityDest)

        val hiddenUiState = mockk<UiState.Hidden>()
        sut.onNewUiState(hiddenUiState)

        sut.visibilityDest shouldBe VisibilityWithCause(
            value = Visibility.Collapsed,
            cause = hiddenUiState,
        )
    }

    @Test
    fun `given main anim 'Collapsed → Visible' is running, when new 'UiState Visible' arrives, then an update of visible UI state is added`() {
        sut = ColorPreviewAnimControllerImpl(uiState = mockk<UiState.Hidden>())

        val visibleUiState1 = mockk<UiState.Visible>()
        sut.onNewUiState(visibleUiState1)
        sut.onMainVisibilityAnimStarted(dest = sut.visibilityDest)
        sut.updatesOfVisibleUiState shouldHaveSize 0

        val visibleUiState2 = mockk<UiState.Visible>()
        sut.onNewUiState(visibleUiState2)

        sut.updatesOfVisibleUiState shouldHaveSize 1
    }

    @Test
    fun `given main anim 'Visible → Collapsed' is running, when new 'UiState Visible' arrives, then an update of visible UI state is added`() {
        sut = ColorPreviewAnimControllerImpl(uiState = mockk<UiState.Visible>())

        val hiddenUiState = mockk<UiState.Hidden>()
        sut.onNewUiState(hiddenUiState)
        sut.onMainVisibilityAnimStarted(dest = sut.visibilityDest)
        sut.updatesOfVisibleUiState shouldHaveSize 0

        val visibleUiState = mockk<UiState.Visible>()
        sut.onNewUiState(visibleUiState)

        sut.updatesOfVisibleUiState shouldHaveSize 1
    }

    @Test
    fun `when SUT is created with initial 'UiState Hidden', then it is reported as stable`() =
        runTest(testDispatcher) {
            val initialState = mockk<UiState.Hidden>()
            sut = ColorPreviewAnimControllerImpl(uiState = initialState)

            sut.flowOfStableReachedUiState.first() shouldBe initialState
        }

    @Test
    fun `when main anim 'Collapsed → Visible' has finished, then reached UI state is reported as stable`() =
        runTest(testDispatcher) {
            sut = ColorPreviewAnimControllerImpl(uiState = mockk<UiState.Hidden>())

            val visibleUiState = mockk<UiState.Visible>()
            sut.onNewUiState(visibleUiState)
            sut.onMainVisibilityAnimStarted(dest = sut.visibilityDest)
            sut.onMainVisibilityAnimFinished(reached = sut.visibilityDest.value)

            sut.flowOfStableReachedUiState.first() shouldBe visibleUiState
        }

    @Test
    fun `when main anim 'Visible → Collapsed' has finished, then reached UI state is reported as stable`() =
        runTest(testDispatcher) {
            sut = ColorPreviewAnimControllerImpl(uiState = mockk<UiState.Visible>())

            val hiddenUiState = mockk<UiState.Hidden>()
            sut.onNewUiState(hiddenUiState)
            sut.onMainVisibilityAnimStarted(dest = sut.visibilityDest)
            sut.onMainVisibilityAnimFinished(reached = sut.visibilityDest.value)

            sut.flowOfStableReachedUiState.first() shouldBe hiddenUiState
        }

    @Test
    fun `when update anim has finished and main anim is not running, then reached UI state is reported as stable`() =
        runTest(testDispatcher) {
            sut = ColorPreviewAnimControllerImpl(uiState = mockk<UiState.Visible>())

            val visibleUiStateUpdate = mockk<UiState.Visible>()
            sut.onNewUiState(visibleUiStateUpdate)
            sut.updatesOfVisibleUiState shouldHaveSize 1 // REFERENCE:Label=0
            sut.onUpdateAnimFinished(update = sut.updatesOfVisibleUiState.single())

            sut.flowOfStableReachedUiState.first() shouldBe visibleUiStateUpdate
        }

    @Test
    fun `given there are multiple ongoing updates, when update anim has finished and main anim is not running, then reached UI state is NOT reported as stable`() =
        runTest(testDispatcher) {
            sut = ColorPreviewAnimControllerImpl(uiState = mockk<UiState.Visible>())

            val visibleUiStateUpdate1 = mockk<UiState.Visible>()
            sut.onNewUiState(visibleUiStateUpdate1)
            sut.updatesOfVisibleUiState shouldHaveSize 1 // REFERENCE:Label=0

            val visibleUiStateUpdate2 = mockk<UiState.Visible>()
            sut.onNewUiState(visibleUiStateUpdate2)
            sut.updatesOfVisibleUiState shouldHaveSize 2 // REFERENCE:Label=0

            sut.onUpdateAnimFinished(update = sut.updatesOfVisibleUiState.first())

            sut.flowOfStableReachedUiState.first() shouldNotBe visibleUiStateUpdate2
        }

    @Test
    fun `when update anim has finished and main anim is running, then reached UI state is NOT reported as stable`() =
        runTest(testDispatcher) {
            sut = ColorPreviewAnimControllerImpl(uiState = mockk<UiState.Hidden>())

            val visibleUiState = mockk<UiState.Visible>()
            sut.onNewUiState(visibleUiState)
            sut.onMainVisibilityAnimStarted(dest = sut.visibilityDest) // started but never finished to keep running

            val visibleUiStateUpdate = mockk<UiState.Visible>()
            sut.onNewUiState(visibleUiStateUpdate)
            sut.updatesOfVisibleUiState shouldHaveSize 1
            sut.onUpdateAnimFinished(update = sut.updatesOfVisibleUiState.single())

            sut.flowOfStableReachedUiState.first() shouldNotBe visibleUiStateUpdate
        }

    @Test
    fun `when main anim 'Collapsed → Visible' has finished, then main UiState is set to the reached one`() {
        sut = ColorPreviewAnimControllerImpl(uiState = mockk<UiState.Hidden>())

        val visibleUiState = mockk<UiState.Visible>()
        sut.onNewUiState(visibleUiState)
        sut.onMainVisibilityAnimStarted(dest = sut.visibilityDest)
        sut.onMainVisibilityAnimFinished(reached = sut.visibilityDest.value)

        sut.mainUiState shouldBe visibleUiState
    }

    @Test
    fun `when update anim has finished, then main UiState is updated`() {
        sut = ColorPreviewAnimControllerImpl(uiState = mockk<UiState.Hidden>())

        val visibleUiState = mockk<UiState.Visible>()
        sut.onNewUiState(visibleUiState)
        sut.onMainVisibilityAnimStarted(dest = sut.visibilityDest)

        val visibleUiStateUpdate = mockk<UiState.Visible>()
        sut.onNewUiState(visibleUiStateUpdate)
        sut.updatesOfVisibleUiState shouldHaveSize 1
        sut.onUpdateAnimFinished(update = sut.updatesOfVisibleUiState.single())

        sut.mainUiState shouldBe visibleUiStateUpdate
    }
}