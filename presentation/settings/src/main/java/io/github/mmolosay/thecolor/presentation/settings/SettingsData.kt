package io.github.mmolosay.thecolor.presentation.settings

import io.github.mmolosay.thecolor.domain.model.UserPreferences.ColorInputType as DomainColorInputType
import io.github.mmolosay.thecolor.domain.model.UserPreferences.UiThemeMode as DomainUiThemeMode

/**
 * Platform-agnostic data provided by ViewModel to 'Settings' View.
 */
data class SettingsData(
    val preferredColorInputType: DomainColorInputType, // it's OK to use some domain models (like enums) in presentation layer
    val changePreferredColorInputType: (DomainColorInputType) -> Unit,
    val appUiThemeMode: LabeledAppUiThemeMode,
    val supportedAppUiThemeModes: List<LabeledAppUiThemeMode>,
    val changeAppUiThemeMode: (LabeledAppUiThemeMode) -> Unit,
) {

    // TODO: abolish
    data class LabeledAppUiThemeMode(
        val mode: DomainUiThemeMode,
        val label: Label,
    ) {
        enum class Label {
            SingleLight,
            SingleDark,
            DualLightDark,
        }
    }
}