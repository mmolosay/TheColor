package io.github.mmolosay.thecolor.presentation.design

import androidx.compose.runtime.compositionLocalOf

val LocalIsNavigationBarLight =
    compositionLocalOf<Boolean> {
        error("CompositionLocal \"LocalIsNavigationBarLight\" doesn't have value by default.")
    }