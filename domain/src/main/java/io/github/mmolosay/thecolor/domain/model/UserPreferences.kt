package io.github.mmolosay.thecolor.domain.model

object UserPreferences {

    enum class ColorInputType {
        Hex, Rgb,
    }

    enum class UiTheme {
        Light, Dark, FollowsSystem
    }
}