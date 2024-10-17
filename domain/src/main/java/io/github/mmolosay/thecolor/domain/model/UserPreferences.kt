package io.github.mmolosay.thecolor.domain.model

// TODO: abolish?
object UserPreferences {

    enum class ColorInputType {
        Hex, Rgb,
    }

    enum class UiTheme {
        Light, Dark,
    }

    sealed interface UiThemeMode {
        data class Single(val theme: UiTheme) : UiThemeMode
        data class Dual(val light: UiTheme, val dark: UiTheme) : UiThemeMode

        companion object {
            val DayNight = Dual(light = UiTheme.Light, dark = UiTheme.Dark)
        }
    }
}