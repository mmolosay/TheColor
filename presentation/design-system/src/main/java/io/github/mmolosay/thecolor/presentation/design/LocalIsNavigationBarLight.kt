package io.github.mmolosay.thecolor.presentation.design

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalIsNavigationBarLight =
    compositionLocalOf<Boolean> {
        error("CompositionLocal \"LocalIsNavigationBarLight\" doesn't have value by default.")
    }

/**
 * Restores default appearance of navigation bar in a [SideEffect].
 *
 * The default appearance is [Color.Transparent] for a background and [LocalIsNavigationBarLight]
 * `current` value for light/dark navigation bar controls.
 */
@Suppress("NOTHING_TO_INLINE") // doesn't work correctly without inlining
@Composable
inline fun RestoreNavigationBarAsSideEffect() {
    val view = LocalView.current
    if (view.isInEditMode) return
    val window = view.context.findActivityContext().window
    val isNavigationBarLight = LocalIsNavigationBarLight.current
    SideEffect {
        window.navigationBarColor = Color.Transparent.toArgb()
        WindowCompat.getInsetsController(window, window.decorView).run {
            isAppearanceLightNavigationBars = isNavigationBarLight
        }
    }
}

/**
 * Changes appearance of navigation bar in a [SideEffect].
 *
 * @param navigationBarColor new background color of a navigation bar.
 * @param isAppearanceLightNavigationBars whether to use dark or light tint for navigation bar controls.
 */
@Suppress("NOTHING_TO_INLINE") // doesn't work correctly without inlining
@Composable
inline fun ChangeNavigationBarAsSideEffect(
    navigationBarColor: Color,
    isAppearanceLightNavigationBars: Boolean,
) {
    val view = LocalView.current
    if (view.isInEditMode) return
    val window = view.context.findActivityContext().window
    SideEffect {
        window.navigationBarColor = navigationBarColor.toArgb()
        WindowCompat.getInsetsController(window, window.decorView).run {
            this.isAppearanceLightNavigationBars = isAppearanceLightNavigationBars
        }
    }
}

@PublishedApi
internal fun Context.findActivityContext(): Activity {
    if (this is Activity) return this
    if (this is ContextWrapper) {
        val wrapped = this.baseContext
        return wrapped.findActivityContext()
    }
    error("This context doesn't belong to Activity")
}