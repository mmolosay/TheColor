package io.github.mmolosay.thecolor.presentation.home

import io.github.mmolosay.thecolor.presentation.api.ColorInt
import io.github.mmolosay.thecolor.presentation.home.ui.FlowOfAnimatedUiState
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewData
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.milliseconds
import io.github.mmolosay.thecolor.presentation.home.ui.HomeAnimState.ColorPreview as ColorPreviewAnimState
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewUiState as UiState

@OptIn(ExperimentalCoroutinesApi::class)
internal class FlowOfAnimatedUiStateTest {

    lateinit var flowOfOriginalData: MutableStateFlow<ColorPreviewData>

    lateinit var flowOfAnimDest: MutableStateFlow<ColorPreviewAnimState>

    val testDispatcher = StandardTestDispatcher()
    val coroutineScope = CoroutineScope(testDispatcher)

    fun sut(): StateFlow<UiState?> =
        FlowOfAnimatedUiState(
            flowOfOriginalData = flowOfOriginalData,
            flowOfAnimDest = flowOfAnimDest,
            coroutineScope = coroutineScope,
        )

    /**
     * GIVEN
     * [flowOfOriginalData] and [flowOfAnimDest] initialized in some way (doesn't matter)
     *
     * WHEN
     * [sut] flow is created
     *
     * THEN
     * SUT flow emits initial `null`.
     */
    @Test
    fun `#0`() = runTest {
        kotlin.run initFlowOfOriginalData@{
            val value = ColorPreviewData(color = null)
            flowOfOriginalData = MutableStateFlow(value)
        }
        kotlin.run initFlowOfAnimDest@{
            val value = mockk<ColorPreviewAnimState> {
                every { visibility } returns ColorPreviewAnimState.Visibility.Hidden
            }
            flowOfAnimDest = MutableStateFlow(value)
        }

        val flowOfUiState = sut()

        // not advancing time here to preserve intial value
        flowOfUiState.value shouldBe null
    }

    /**
     * GIVEN
     * - [flowOfOriginalData] has no color
     * - [flowOfAnimDest] has [ColorPreviewAnimState.Visibility.Hidden]
     *
     * WHEN
     * [sut] flow is created
     *
     * THEN
     * SUT flow value is [UiState.Hidden].
     */
    @Test
    fun `#1`() = runTest {
        kotlin.run initFlowOfOriginalData@{
            val value = ColorPreviewData(color = null)
            flowOfOriginalData = MutableStateFlow(value)
        }
        kotlin.run initFlowOfAnimDest@{
            val value = mockk<ColorPreviewAnimState> {
                every { visibility } returns ColorPreviewAnimState.Visibility.Hidden
            }
            flowOfAnimDest = MutableStateFlow(value)
        }

        val flowOfUiState = sut()

        testDispatcher.scheduler.advanceUntilIdle()
        flowOfUiState.value shouldBe UiState.Hidden
    }

    /**
     * If [flowOfOriginalData] emits value dX first,
     * and then [flowOfAnimDest] emits value adY,
     * then SUT will process dX only after adY arrives.
     * Thus, dX satisfies adY and dX is accepted (emitted from SUT flow)
     *
     * See also test #3.
     *
     * GIVEN
     * - [flowOfOriginalData] has no color
     * - [flowOfAnimDest] has [ColorPreviewAnimState.Visibility.Hidden]
     *
     * WHEN
     * 1. [sut] flow is created
     * 2. [flowOfOriginalData] emits [ColorPreviewData] with color X
     * 3. [flowOfAnimDest] emits [ColorPreviewAnimState.Visibility.Visible]
     *
     * THEN
     * SUT flow value is [UiState.Visible] with color X.
     */
    @Test
    fun `#2`() = runTest {
        kotlin.run initFlowOfOriginalData@{
            val value = ColorPreviewData(color = null)
            flowOfOriginalData = MutableStateFlow(value)
        }
        kotlin.run initFlowOfAnimDest@{
            val value = mockk<ColorPreviewAnimState> {
                every { visibility } returns ColorPreviewAnimState.Visibility.Hidden
            }
            flowOfAnimDest = MutableStateFlow(value)
        }

        val flowOfUiState = sut()

        flowOfUiState.value shouldBe null // from test #0
        testDispatcher.scheduler.advanceUntilIdle()
        flowOfUiState.value shouldBe UiState.Hidden // from test #1
        kotlin.run {
            val value = ColorPreviewData(color = ColorInt(0x0))
            flowOfOriginalData.emit(value)
        }
        testDispatcher.scheduler.advanceUntilIdle()
        flowOfUiState.value shouldBe UiState.Hidden // nothing has changed
        kotlin.run {
            val value = mockk<ColorPreviewAnimState> {
                every { visibility } returns ColorPreviewAnimState.Visibility.Visible
            }
            flowOfAnimDest.emit(value)
        }
        testDispatcher.scheduler.advanceUntilIdle()
        flowOfUiState.value shouldBe UiState.Visible(color = ColorInt(0x0))
    }

    /**
     * Same as test #2, but with steps #2 and #3 in WHEN section swapped.
     *
     * GIVEN
     * - [flowOfOriginalData] has no color
     * - [flowOfAnimDest] has [ColorPreviewAnimState.Visibility.Hidden]
     *
     * WHEN
     * 1. [sut] flow is created
     * 2. [flowOfAnimDest] emits [ColorPreviewAnimState.Visibility.Visible]
     * 3. [flowOfOriginalData] emits [ColorPreviewData] with color X
     *
     * THEN
     * SUT flow value is [UiState.Visible] with color X.
     */
    @Test
    fun `#3`() = runTest {
        kotlin.run initFlowOfOriginalData@{
            val value = ColorPreviewData(color = null)
            flowOfOriginalData = MutableStateFlow(value)
        }
        kotlin.run initFlowOfAnimDest@{
            val value = mockk<ColorPreviewAnimState> {
                every { visibility } returns ColorPreviewAnimState.Visibility.Hidden
            }
            flowOfAnimDest = MutableStateFlow(value)
        }

        val flowOfUiState = sut()

        // WHEN #2
        kotlin.run {
            val value = mockk<ColorPreviewAnimState> {
                every { visibility } returns ColorPreviewAnimState.Visibility.Visible
            }
            flowOfAnimDest.emit(value)
        }
        testDispatcher.scheduler.advanceUntilIdle()
        flowOfUiState.value shouldBe UiState.Hidden // nothing has changed
        // WHEN #3
        kotlin.run {
            val value = ColorPreviewData(color = ColorInt(0x0))
            flowOfOriginalData.emit(value)
        }
        testDispatcher.scheduler.advanceUntilIdle()
        flowOfUiState.value shouldBe UiState.Visible(color = ColorInt(0x0))
    }

    /**
     * If [flowOfOriginalData] emits value that (in translation to [UiState]) satisfies
     * current value of [flowOfAnimDest], then it is accepted (emitted from SUT flow).
     *
     * GIVEN
     * - [flowOfOriginalData] has color X
     * - [flowOfAnimDest] has [ColorPreviewAnimState.Visibility.Visible]
     *
     * WHEN
     * 1. [sut] flow is created
     * 2. [flowOfOriginalData] emits [ColorPreviewData] with color Y
     *
     * THEN
     * SUT flow value is [UiState.Visible] with color Y.
     */
    @Test
    fun `#4`() = runTest {
        kotlin.run initFlowOfOriginalData@{
            val value = ColorPreviewData(color = ColorInt(0x0))
            flowOfOriginalData = MutableStateFlow(value)
        }
        kotlin.run initFlowOfAnimDest@{
            val value = mockk<ColorPreviewAnimState> {
                every { visibility } returns ColorPreviewAnimState.Visibility.Visible
            }
            flowOfAnimDest = MutableStateFlow(value)
        }

        val flowOfUiState = sut()
        testDispatcher.scheduler.advanceUntilIdle()
        // WHEN #2
        kotlin.run {
            val value = ColorPreviewData(color = ColorInt(0x1))
            flowOfOriginalData.emit(value)
        }
        testDispatcher.scheduler.advanceUntilIdle()
        flowOfUiState.value shouldBe UiState.Visible(color = ColorInt(0x1))
    }

    /**
     * GIVEN
     * - [flowOfOriginalData] has color X
     * - [flowOfAnimDest] has [ColorPreviewAnimState.Visibility.Visible]
     *
     * WHEN
     * 1. [sut] flow is created
     * 2. [flowOfOriginalData] emits [ColorPreviewData] with color Y
     * 3. [flowOfAnimDest] emits [ColorPreviewAnimState.Visibility.Hidden]
     * 4. [flowOfOriginalData] emits [ColorPreviewData] with no color
     *
     * THEN
     * SUT flow value is [UiState.Hidden].
     */
    @Test
    fun `#5`() = runTest {
        kotlin.run initFlowOfOriginalData@{
            val value = ColorPreviewData(color = ColorInt(0x0))
            flowOfOriginalData = MutableStateFlow(value)
        }
        kotlin.run initFlowOfAnimDest@{
            val value = mockk<ColorPreviewAnimState> {
                every { visibility } returns ColorPreviewAnimState.Visibility.Visible
            }
            flowOfAnimDest = MutableStateFlow(value)
        }

        val flowOfUiState = sut()
        testDispatcher.scheduler.advanceUntilIdle()
        // WHEN #2
        kotlin.run {
            val value = ColorPreviewData(color = ColorInt(0x1))
            flowOfOriginalData.emit(value)
        }
        testDispatcher.scheduler.advanceTimeBy(25.milliseconds)
        // WHEN #3
        kotlin.run {
            val value = mockk<ColorPreviewAnimState> {
                every { visibility } returns ColorPreviewAnimState.Visibility.Hidden
            }
            flowOfAnimDest.emit(value)
        }
        testDispatcher.scheduler.advanceUntilIdle()
        flowOfUiState.value shouldBe UiState.Visible(color = ColorInt(0x0))
        // WHEN #4
        kotlin.run {
            val value = ColorPreviewData(color = null)
            flowOfOriginalData.emit(value)
        }
        testDispatcher.scheduler.advanceUntilIdle()
        flowOfUiState.value shouldBe UiState.Hidden
    }
}