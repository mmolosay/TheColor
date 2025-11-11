package io.github.mmolosay.thecolor.presentation.common.compose

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent

/**
 * Adds specified [modifier] to the receiver [Modifier] chain if [condition] is `true`.
 * Else, returns the receiver [Modifier] without changes.
 */
inline fun Modifier.thenIf(
    condition: Boolean,
    modifier: Modifier.() -> Modifier,
): Modifier =
    if (condition) {
        this.modifier()
    } else {
        this
    }

/**
 * Only draws the element this modifier is applied to if [shouldDraw] is `true`.
 *
 * This modifier is useful when the element’s final appearance is calculated over multiple frames.
 * Since intermediate versions of the element may change every frame and appear flickering,
 * this modifier skips drawing until the final appearance is ready.
 *
 * Example: calculating custom `contentPadding` for `LazyRow` / `LazyColumn`:
 * On the 1st frame (initial composition), `LazyListState.layoutInfo` doesn't have measurements populated (all zeros).
 * On the 2nd frame, the measurements are populated, which now you use to calculate custom `contentPadding`.
 * On the 3rd frame, the final appearance (with calculated padding) is ready, and the element is displayed.
 */
fun Modifier.drawIf(shouldDraw: Boolean): Modifier =
    this.drawWithContent {
        if (shouldDraw) drawContent()
    }