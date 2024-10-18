package io.github.mmolosay.thecolor.presentation.settings

import io.github.mmolosay.thecolor.domain.model.UserPreferences.ColorInputType as DomainColorInputType
import io.github.mmolosay.thecolor.domain.model.UserPreferences.UiColorSchemeMode as DomainUiColorSchemeMode

/**
 * Platform-agnostic data provided by ViewModel to 'Settings' View.
 */
data class SettingsData(
    val preferredColorInputType: DomainColorInputType, // it's OK to use some domain models (like enums) in presentation layer
    val changePreferredColorInputType: (DomainColorInputType) -> Unit,
    val appUiThemeMode: DomainUiColorSchemeMode,
    val supportedAppUiThemeModes: List<DomainUiColorSchemeMode>,
    val changeAppUiThemeMode: (DomainUiColorSchemeMode) -> Unit,
)