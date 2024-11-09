package io.github.mmolosay.thecolor.domain.model

/**
 * A collection of models that represent user-selected options.
 */
object UserPreferences {

    enum class UiColorScheme {
        Light, Dark,
    }

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
}