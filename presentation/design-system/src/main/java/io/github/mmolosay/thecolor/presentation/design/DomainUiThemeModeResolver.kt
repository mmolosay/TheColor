package io.github.mmolosay.thecolor.presentation.design

import io.github.mmolosay.thecolor.domain.model.UserPreferences
import io.github.mmolosay.thecolor.presentation.design.UiThemeMapper.toPresentation
import io.github.mmolosay.thecolor.domain.model.UserPreferences.UiThemeMode as DomainUiThemeMode

/**
 * Processes [DomainUiThemeMode] of Domain layer into a corresponding [UiTheme] of Presentation layer.
 */
object DomainUiThemeModeResolver {

    fun DomainUiThemeMode.resolve(
        isSystemInDarkMode: Boolean,
    ): UiTheme =
        when (this) {
            is UserPreferences.UiThemeMode.Single -> {
                this.theme.toPresentation()
            }
            is UserPreferences.UiThemeMode.Dual -> {
                val theme = if (isSystemInDarkMode) this.dark else this.light
                theme.toPresentation()
            }
        }

}