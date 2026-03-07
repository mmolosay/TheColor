package io.github.mmolosay.thecolor.domain.user.preferences

/**
 * A collection of models that represent user-selected options.
 */
object UserPreferences {

    enum class UiColorScheme {
        Light,
        Dark,
        Jungle,
        Midnight,
    }

    /**
     * A set of color schemes to choose from when resolving a color scheme to use in app's UI.
     * [light] is used when device's Dark mode is OFF. [dark] when it's ON.
     *
     * Both values may be the same. In this case, a set is considered a singleton and color scheme
     * is not sensitive to device's Dark mode.
     */
    data class UiColorSchemeSet(
        val light: UiColorScheme,
        val dark: UiColorScheme,
    ) {
        companion object {
            val DayNight = UiColorSchemeSet(light = UiColorScheme.Light, dark = UiColorScheme.Dark)
        }
    }

    fun UiColorScheme.asSingletonSet(): UiColorSchemeSet =
        UiColorSchemeSet(light = this, dark = this)

    // https://en.wikipedia.org/wiki/Singleton_(mathematics)
    fun UiColorSchemeSet.isSingleton(): Boolean =
        (this.light == this.dark)

    fun UiColorSchemeSet.single(): UiColorScheme =
        this.light

    @JvmInline
    value class DynamicUiColors(
        val enabled: Boolean,
    )

    @JvmInline
    value class ResumeFromLastSearchedColorOnStartup(
        val enabled: Boolean,
    )

    @JvmInline
    value class SmartBackspace(
        val enabled: Boolean,
    )

    @JvmInline
    value class SelectAllTextOnTextFieldFocus(
        val enabled: Boolean,
    )

    @JvmInline
    value class AutoProceedWithRandomizedColors(
        val enabled: Boolean,
    )
}