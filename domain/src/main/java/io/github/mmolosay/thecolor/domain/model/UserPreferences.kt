package io.github.mmolosay.thecolor.domain.model

// TODO: abolish?
object UserPreferences {

    enum class ColorInputType {
        Hex, Rgb,
    }

    enum class UiColorScheme {
        Light, Dark,
    }

    sealed interface UiColorSchemeMode {
        data class Single(val scheme: UiColorScheme) : UiColorSchemeMode
        data class Dual(val light: UiColorScheme, val dark: UiColorScheme) : UiColorSchemeMode

        companion object {
            val DayNight = Dual(light = UiColorScheme.Light, dark = UiColorScheme.Dark)
        }
    }
}