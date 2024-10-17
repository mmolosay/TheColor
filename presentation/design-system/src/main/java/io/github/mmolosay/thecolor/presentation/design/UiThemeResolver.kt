package io.github.mmolosay.thecolor.presentation.design

/**
 * Defines a strategy of obtaining a [UiTheme] for a provided [Brightness][UiTheme.Brightness].
 */
fun interface UiThemeResolver {
    fun resolve(brightness: UiTheme.Brightness): UiTheme
}

/**
 * A [UiThemeResolver] that chooses between [UiTheme.Light] and [UiTheme.Dark].
 */
object DayNightUiThemeResolver : UiThemeResolver {

    override fun resolve(brightness: UiTheme.Brightness): UiTheme =
        when (brightness) {
            UiTheme.Brightness.Light -> UiTheme.Light
            UiTheme.Brightness.Dark -> UiTheme.Dark
        }
}