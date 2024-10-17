package io.github.mmolosay.thecolor.presentation.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import io.github.mmolosay.thecolor.domain.model.UserPreferences.UiTheme as DomainUiTheme

enum class UiTheme {
    Light,
    Dark,
    DayNight,
    ;

    enum class Tone {
        Light, Dark
    }
}

fun UiTheme.tone(isSystemInDarkTheme: Boolean) =
    when (this) {
        UiTheme.Light -> UiTheme.Tone.Light
        UiTheme.Dark -> UiTheme.Tone.Dark
        UiTheme.DayNight -> when (isSystemInDarkTheme) {
            true -> UiTheme.Tone.Dark
            false -> UiTheme.Tone.Light
        }
    }

@Composable
fun UiTheme.isDefaultNavigationBarLight(): Boolean {
    val uiThemeTone = this.tone(isSystemInDarkTheme())
    return when (uiThemeTone) {
        UiTheme.Tone.Light -> true
        UiTheme.Tone.Dark -> false
    }
}

fun DomainUiTheme.toPresentation(): UiTheme =
    when (this) {
        DomainUiTheme.Light -> UiTheme.Light
        DomainUiTheme.Dark -> UiTheme.Dark
        DomainUiTheme.FollowsSystem -> UiTheme.DayNight
    }