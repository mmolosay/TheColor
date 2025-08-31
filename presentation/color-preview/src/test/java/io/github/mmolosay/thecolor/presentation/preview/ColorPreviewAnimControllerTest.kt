package io.github.mmolosay.thecolor.presentation.preview

import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewAnimController.UiStateWithVisibility
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewAnimController.View
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewAnimState.Visibility
import io.github.mmolosay.thecolor.testing.clearMocksOnlyRecordedCalls
import io.github.mmolosay.thecolor.utils.doNothing
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.called
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Test
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewUiState as UiState

@OptIn(ExperimentalCoroutinesApi::class)
class ColorPreviewAnimControllerTest {

    lateinit var sut: ColorPreviewAnimController

    @Test
    fun `given initial 'UiState Hidden', when new 'UiState Visible' arrives and there is no View, then UiState is updated`() {
        sut = ColorPreviewAnimController(uiState = mockk<UiState.Hidden>())

        val visibleUiState = mockk<UiState.Visible>()
        sut.onNewUiState(newUiState = visibleUiState)

        sut.uiStateWithVisibility.uiState shouldBe visibleUiState
    }

    @Test
    fun `given initial 'UiState Visible', when new 'UiState Hidden' arrives and there is no View, then UiState is updated`() {
        sut = ColorPreviewAnimController(uiState = mockk<UiState.Visible>())

        val hiddenUiState = mockk<UiState.Hidden>()
        sut.onNewUiState(newUiState = hiddenUiState)

        sut.uiStateWithVisibility.uiState shouldBe hiddenUiState
    }

    @Test
    fun `given initial 'UiState Visible', when new 'UiState Visible' arrives and there is no View, then UiState is updated`() {
        sut = ColorPreviewAnimController(uiState = mockk<UiState.Visible>())

        val visibleUiState = mockk<UiState.Visible>()
        sut.onNewUiState(newUiState = visibleUiState)

        sut.uiStateWithVisibility.uiState shouldBe visibleUiState
    }

    @Test
    fun `given initial 'UiState Visible' and there is no View, when View is set, then no animation is started`() {
        sut = ColorPreviewAnimController(uiState = mockk<UiState.Visible>())

        val view = mockk<View>(relaxed = true)
        sut.setView(view)

        verify {
            view wasNot called // no "animate" interactions happened with the View
        }
    }

    /**
     * Tests that when [View] is removed, any ongoing animation is "rushed" to finish
     * and the dest [UiState] is applied immediately.
     */
    @Test
    fun `given initial 'UiState Hidden' and there is a View set, when new 'UiState Visible' arrives and animation starts and View is removed, then UiState is updated`() {
        sut = ColorPreviewAnimController(uiState = mockk<UiState.Hidden>())
        val view = mockk<View>(relaxed = true)
        sut.setView(view)

        val visibleUiState = mockk<UiState.Visible>()
        sut.onNewUiState(newUiState = visibleUiState)
        // REFERENCE:Label=1
        verify {
            view.animateVisibility(dest = any(), onAnimStarted = any(), onAnimFinished = any())
        }
        sut.setView(null)

        sut.uiStateWithVisibility.uiState shouldBe visibleUiState
    }

    @Test // ANCHOR:Label=1
    fun `given initial 'UiState Hidden', when new 'UiState Visible' arrives, then visibility animation starts`() {
        sut = ColorPreviewAnimController(uiState = mockk<UiState.Hidden>())
        val view = mockk<View>(relaxed = true)
        sut.setView(view)

        val newUiState = mockk<UiState.Visible>()
        sut.onNewUiState(newUiState)

        verify(exactly = 1) {
            view.animateVisibility(
                dest = UiStateWithVisibility(uiState = newUiState, visibility = Visibility.Expanded),
                onAnimStarted = any(),
                onAnimFinished = any(),
            )
        }
    }

    @Test
    fun `when visibility anim 'Collapsed → Visible' has finished, then UiState is updated`() {
        sut = ColorPreviewAnimController(uiState = mockk<UiState.Hidden>())
        val view = mockk<View>(relaxed = true)
        sut.setView(view)

        val visibleUiState = mockk<UiState.Visible>()
        sut.onNewUiState(visibleUiState)
        val onAnimStarted = slot<() -> Unit>()
        val onAnimFinished = slot<() -> Unit>()
        // REFERENCE:Label=1
        verify(exactly = 1) {
            view.animateVisibility(
                dest = UiStateWithVisibility(uiState = visibleUiState, visibility = Visibility.Expanded),
                onAnimStarted = capture(onAnimStarted),
                onAnimFinished = capture(onAnimFinished),
            )
        }
        onAnimStarted.captured() // report that animation in UI has started
        onAnimFinished.captured() // report that animation in UI has finished

        sut.uiStateWithVisibility.uiState shouldBe visibleUiState
    }

    @Test
    fun `when 'update of visible UiState' anim has finished, then UiState is updated`() {
        sut = ColorPreviewAnimController(uiState = mockk<UiState.Visible>())
        val view = mockk<View>(relaxed = true)
        sut.setView(view)

        val visibleUiState = mockk<UiState.Visible>()
        sut.onNewUiState(visibleUiState)
        val onAnimStarted = slot<() -> Unit>()
        val onAnimFinished = slot<() -> Unit>()
        // REFERENCE:Label=0
        verify(exactly = 1) {
            view.animateUpdateOfVisibleUiState(
                uiState = visibleUiState,
                onAnimStarted = capture(onAnimStarted),
                onAnimFinished = capture(onAnimFinished),
            )
        }
        onAnimStarted.captured() // report that animation in UI has started
        onAnimFinished.captured() // report that animation in UI has finished

        sut.uiStateWithVisibility.uiState shouldBe visibleUiState
    }

    @Test // ANCHOR:Label=2
    fun `given initial 'UiState Visible', when new 'UiState Hidden' arrives, then visibility animation starts`() {
        sut = ColorPreviewAnimController(uiState = mockk<UiState.Visible>())
        val view = mockk<View>(relaxed = true)
        sut.setView(view)

        val newUiState = mockk<UiState.Hidden>()
        sut.onNewUiState(newUiState)

        verify(exactly = 1) {
            view.animateVisibility(
                dest = UiStateWithVisibility(uiState = newUiState, visibility = Visibility.Collapsed),
                onAnimStarted = any(),
                onAnimFinished = any(),
            )
        }
    }

    @Test // ANCHOR:Label=0
    fun `given initial 'UiState Visible', when new 'UiState Visible' arrives, then 'update of visible UiState' animation starts`() {
        sut = ColorPreviewAnimController(uiState = mockk<UiState.Visible>())
        val view = mockk<View>(relaxed = true)
        sut.setView(view)

        val newUiState = mockk<UiState.Visible>()
        sut.onNewUiState(newUiState)

        verify(exactly = 1) {
            view.animateUpdateOfVisibleUiState(
                uiState = newUiState,
                onAnimStarted = any(),
                onAnimFinished = any(),
            )
        }
    }

    @Test
    fun `given visibility anim 'Collapsed → Visible' is running, when new 'UiState Hidden' arrives, then new visibility animation starts`() {
        sut = ColorPreviewAnimController(uiState = mockk<UiState.Hidden>())
        val view = mockk<View>(relaxed = true)
        sut.setView(view)

        val visibleUiState = mockk<UiState.Visible>()
        sut.onNewUiState(visibleUiState)
        val onAnimStarted = slot<() -> Unit>()
        // REFERENCE:Label=1
        verify(exactly = 1) {
            view.animateVisibility(
                dest = UiStateWithVisibility(uiState = visibleUiState, visibility = Visibility.Expanded),
                onAnimStarted = capture(onAnimStarted),
                onAnimFinished = any(),
            )
        }
        clearMocksOnlyRecordedCalls(view)
        onAnimStarted.captured() // report that animation in UI has started

        val hiddenUiState = mockk<UiState.Hidden>()
        sut.onNewUiState(hiddenUiState)

        verify(exactly = 1) {
            view.animateVisibility(
                dest = UiStateWithVisibility(uiState = hiddenUiState, visibility = Visibility.Collapsed),
                onAnimStarted = any(),
                onAnimFinished = any(),
            )
        }
    }

    @Test // ANCHOR:Label=3
    fun `given visibility anim 'Collapsed → Visible' is running, when new 'UiState Visible' arrives, then 'update of visible UiState' animation starts`() {
        sut = ColorPreviewAnimController(uiState = mockk<UiState.Hidden>())
        val view = mockk<View>(relaxed = true)
        sut.setView(view)

        val visibleUiState1 = mockk<UiState.Visible>()
        sut.onNewUiState(visibleUiState1)
        val onAnimStarted = slot<() -> Unit>()
        // REFERENCE:Label=1
        verify(exactly = 1) {
            view.animateVisibility(
                dest = UiStateWithVisibility(uiState = visibleUiState1, visibility = Visibility.Expanded),
                onAnimStarted = capture(onAnimStarted),
                onAnimFinished = any(),
            )
        }
        verify(exactly = 0) {
            view.animateUpdateOfVisibleUiState(uiState = any(), onAnimStarted = any(), onAnimFinished = any())
        }
        clearMocksOnlyRecordedCalls(view)
        onAnimStarted.captured() // report that animation in UI has started

        val visibleUiState2 = mockk<UiState.Visible>()
        sut.onNewUiState(visibleUiState2)

        verify(exactly = 1) {
            view.animateUpdateOfVisibleUiState(uiState = visibleUiState2, onAnimStarted = any(), onAnimFinished = any())
        }
    }

    @Test
    fun `given visibility anim 'Visible → Collapsed' is running, when new 'UiState Visible' arrives, then 'update of visible UiState' animation starts`() {
        sut = ColorPreviewAnimController(uiState = mockk<UiState.Visible>())
        val view = mockk<View>(relaxed = true)
        sut.setView(view)

        val hiddenUiState = mockk<UiState.Hidden>()
        sut.onNewUiState(hiddenUiState)
        val onAnimStarted = slot<() -> Unit>()
        // REFERENCE:Label=2
        verify(exactly = 1) {
            view.animateVisibility(
                dest = UiStateWithVisibility(uiState = hiddenUiState, visibility = Visibility.Collapsed),
                onAnimStarted = capture(onAnimStarted),
                onAnimFinished = any(),
            )
        }
        verify(exactly = 0) {
            view.animateUpdateOfVisibleUiState(uiState = any(), onAnimStarted = any(), onAnimFinished = any())
        }
        clearMocksOnlyRecordedCalls(view)
        onAnimStarted.captured() // report that animation in UI has started

        val visibleUiState = mockk<UiState.Visible>()
        sut.onNewUiState(visibleUiState)

        verify(exactly = 1) {
            view.animateUpdateOfVisibleUiState(uiState = visibleUiState, onAnimStarted = any(), onAnimFinished = any())
        }
    }

    @Test
    fun `when SUT is created with initial 'UiState Hidden', then it is reported as stable`() {
        val initialState = mockk<UiState.Hidden>()
        sut = ColorPreviewAnimController(uiState = initialState)

        sut.lastStableReachedUiState shouldBe initialState
    }

    @Test
    fun `when visibility anim 'Collapsed → Visible' has finished, then reached UiState is reported as stable`() {
        sut = ColorPreviewAnimController(uiState = mockk<UiState.Hidden>())
        val view = mockk<View>(relaxed = true)
        sut.setView(view)

        val visibleUiState = mockk<UiState.Visible>()
        sut.onNewUiState(visibleUiState)
        val onAnimStarted = slot<() -> Unit>()
        val onAnimFinished = slot<() -> Unit>()
        // REFERENCE:Label=1
        verify(exactly = 1) {
            view.animateVisibility(
                dest = UiStateWithVisibility(uiState = visibleUiState, visibility = Visibility.Expanded),
                onAnimStarted = capture(onAnimStarted),
                onAnimFinished = capture(onAnimFinished),
            )
        }
        onAnimStarted.captured() // report that animation in UI has started
        onAnimFinished.captured() // report that animation in UI has finished

        sut.lastStableReachedUiState shouldBe visibleUiState
    }

    @Test
    fun `when visibility anim 'Visible → Collapsed' has finished, then reached UiState is reported as stable`() {
        sut = ColorPreviewAnimController(uiState = mockk<UiState.Visible>())
        val view = mockk<View>(relaxed = true)
        sut.setView(view)

        val hiddenUiState = mockk<UiState.Hidden>()
        sut.onNewUiState(hiddenUiState)
        val onAnimStarted = slot<() -> Unit>()
        val onAnimFinished = slot<() -> Unit>()
        // REFERENCE:Label=2
        verify(exactly = 1) {
            view.animateVisibility(
                dest = UiStateWithVisibility(uiState = hiddenUiState, visibility = Visibility.Collapsed),
                onAnimStarted = capture(onAnimStarted),
                onAnimFinished = capture(onAnimFinished),
            )
        }
        onAnimStarted.captured() // report that animation in UI has started
        onAnimFinished.captured() // report that animation in UI has finished

        sut.lastStableReachedUiState shouldBe hiddenUiState
    }

    @Test
    fun `when 'update of visible UiState' anim has finished and visibility anim is not running, then reached UiState is reported as stable`() {
        sut = ColorPreviewAnimController(uiState = mockk<UiState.Hidden>())
        val view = mockk<View>(relaxed = true)
        sut.setView(view)

        val visibleUiState1 = mockk<UiState.Visible>()
        sut.onNewUiState(visibleUiState1)
        val onVisibilityAnimStarted = slot<() -> Unit>()
        val onVisibilityAnimFinished = slot<() -> Unit>()
        // REFERENCE:Label=1
        verify(exactly = 1) {
            view.animateVisibility(
                dest = UiStateWithVisibility(uiState = visibleUiState1, visibility = Visibility.Expanded),
                onAnimStarted = capture(onVisibilityAnimStarted),
                onAnimFinished = capture(onVisibilityAnimFinished),
            )
        }
        onVisibilityAnimStarted.captured() // report that animation in UI has started
        doNothing() // don't report that it has finished yet

        val visibleUiState2 = mockk<UiState.Visible>()
        sut.onNewUiState(visibleUiState2)
        val onUpdateOfVisibleUiStateAnimStarted = slot<() -> Unit>()
        val onUpdateOfVisibleUiStateAnimFinished = slot<() -> Unit>()
        // REFERENCE:Label=3
        verify(exactly = 1) {
            view.animateUpdateOfVisibleUiState(
                uiState = visibleUiState2,
                onAnimStarted = capture(onUpdateOfVisibleUiStateAnimStarted),
                onAnimFinished = capture(onUpdateOfVisibleUiStateAnimFinished),
            )
        }
        onUpdateOfVisibleUiStateAnimStarted.captured() // report that animation in UI has started
        onVisibilityAnimFinished.captured() // now first visibility animation has finished
        onUpdateOfVisibleUiStateAnimFinished.captured() // and now the second one has finished

        sut.lastStableReachedUiState shouldBe visibleUiState2
    }

    @Test
    fun `given there are multiple ongoing 'update of visible UiState' anims, when one of them has finished and visibility anim is not running, then reached UiState is NOT reported as stable`() {
        sut = ColorPreviewAnimController(uiState = mockk<UiState.Visible>())
        val view = mockk<View>(relaxed = true)
        sut.setView(view)

        val visibleUiState1 = mockk<UiState.Visible>()
        sut.onNewUiState(visibleUiState1)
        val onAnimStarted1 = slot<() -> Unit>()
        val onAnimFinished1 = slot<() -> Unit>()
        // REFERENCE:Label=0
        verify(exactly = 1) {
            view.animateUpdateOfVisibleUiState(
                uiState = visibleUiState1,
                onAnimStarted = capture(onAnimStarted1),
                onAnimFinished = capture(onAnimFinished1),
            )
        }
        onAnimStarted1.captured() // report that animation in UI has started
        doNothing() // don't report that it has finished yet

        val visibleUiState2 = mockk<UiState.Visible>()
        sut.onNewUiState(visibleUiState2)
        val onAnimStarted2 = slot<() -> Unit>()
        val onAnimFinished2 = slot<() -> Unit>()
        // REFERENCE:Label=0
        verify(exactly = 1) {
            view.animateUpdateOfVisibleUiState(
                uiState = visibleUiState2,
                onAnimStarted = capture(onAnimStarted2),
                onAnimFinished = capture(onAnimFinished2),
            )
        }
        onAnimStarted2.captured() // report that animation in UI has started
        onAnimFinished1.captured() // now first visibility animation has finished
        doNothing() // but second one is still running

        sut.lastStableReachedUiState shouldNotBe visibleUiState2
    }

    @Test
    fun `when 'update of visible UiState' anim has finished and visibility anim is running, then reached UiState is NOT reported as stable`() {
        sut = ColorPreviewAnimController(uiState = mockk<UiState.Hidden>())
        val view = mockk<View>(relaxed = true)
        sut.setView(view)

        val visibleUiState1 = mockk<UiState.Visible>()
        sut.onNewUiState(visibleUiState1)
        val onAnimStarted1 = slot<() -> Unit>()
        val onAnimFinished1 = slot<() -> Unit>()
        // REFERENCE:Label=1
        verify(exactly = 1) {
            view.animateVisibility(
                dest = UiStateWithVisibility(uiState = visibleUiState1, visibility = Visibility.Expanded),
                onAnimStarted = capture(onAnimStarted1),
                onAnimFinished = capture(onAnimFinished1),
            )
        }
        onAnimStarted1.captured() // report that animation in UI has started
        doNothing() // don't report that it has finished yet

        val visibleUiState2 = mockk<UiState.Visible>()
        sut.onNewUiState(visibleUiState2)
        val onAnimStarted2 = slot<() -> Unit>()
        val onAnimFinished2 = slot<() -> Unit>()
        // REFERENCE:Label=0
        verify(exactly = 1) {
            view.animateUpdateOfVisibleUiState(
                uiState = visibleUiState2,
                onAnimStarted = capture(onAnimStarted2),
                onAnimFinished = capture(onAnimFinished2),
            )
        }
        onAnimStarted2.captured() // report that animation in UI has started
        onAnimFinished2.captured() // report that animation in UI has finished
        doNothing() // but first one is still running

        sut.lastStableReachedUiState shouldNotBe visibleUiState2
    }
}