package io.github.mmolosay.thecolor.presentation.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import io.github.mmolosay.thecolor.domain.model.UserPreferences.UiTheme as DomainUiTheme

enum class UiTheme {
    Light,
    Dark,
    DayNight,
    ;

    enum class Brightness {
        Light, Dark;
    }
}

fun UiTheme.brightness(isSystemInDarkTheme: Boolean) =
    when (this) {
        UiTheme.Light -> UiTheme.Brightness.Light
        UiTheme.Dark -> UiTheme.Brightness.Dark
        UiTheme.DayNight -> when (isSystemInDarkTheme) {
            true -> UiTheme.Brightness.Dark
            false -> UiTheme.Brightness.Light
        }
    }

@Composable
fun UiTheme.isDefaultNavigationBarLight(): Boolean {
    val uiThemeTone = this.brightness(isSystemInDarkTheme())
    return when (uiThemeTone) {
        UiTheme.Brightness.Light -> true
        UiTheme.Brightness.Dark -> false
    }
}

fun DomainUiTheme.toPresentation(): UiTheme =
    when (this) {
        DomainUiTheme.Light -> UiTheme.Light
        DomainUiTheme.Dark -> UiTheme.Dark
        DomainUiTheme.FollowsSystem -> UiTheme.DayNight
    }