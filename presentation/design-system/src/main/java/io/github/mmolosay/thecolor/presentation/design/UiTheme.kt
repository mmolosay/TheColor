package io.github.mmolosay.thecolor.presentation.design

enum class UiTheme {
    Light,
    Dark,
    DayNight,
}

fun UiTheme.isLight(isSystemInDarkTheme: Boolean) =
    when (this) {
        UiTheme.Light -> false
        UiTheme.Dark -> true
        UiTheme.DayNight -> !isSystemInDarkTheme
    }