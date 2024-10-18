package io.github.mmolosay.thecolor.presentation.design

/**
 * Defines a strategy of obtaining a [UiTheme] for a provided [Brightness][UiTheme.Brightness].
 * Brightness is usually inferred from system UI mode (see [systemBrightness]).
 */
fun interface UiThemeResolver {
    fun resolve(brightness: Brightness): UiTheme
}

/**
 * A [UiThemeResolver] that chooses between [UiTheme.Light] and [UiTheme.Dark].
 */
object DayNightUiThemeResolver : UiThemeResolver {

    override fun resolve(brightness: Brightness): UiTheme =
        when (brightness) {
            Brightness.Light -> UiTheme.Light
            Brightness.Dark -> UiTheme.Dark
        }
}