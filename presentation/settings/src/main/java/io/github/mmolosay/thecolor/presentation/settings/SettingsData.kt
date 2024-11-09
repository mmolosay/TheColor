package io.github.mmolosay.thecolor.presentation.settings

import io.github.mmolosay.thecolor.domain.model.ColorInputType as DomainColorInputType
import io.github.mmolosay.thecolor.domain.model.UserPreferences.UiColorSchemeSet as DomainUiColorSchemeSet

/**
 * Platform-agnostic data provided by ViewModel to 'Settings' View.
 */
data class SettingsData(
    val preferredColorInputType: DomainColorInputType, // it's OK to use some domain models (like enums) in presentation layer
    val changePreferredColorInputType: (DomainColorInputType) -> Unit,
    val appUiColorSchemeSet: DomainUiColorSchemeSet,
    val supportedAppUiColorSchemeSets: List<DomainUiColorSchemeSet>,
    val changeAppUiColorSchemeSet: (DomainUiColorSchemeSet) -> Unit,
    val isResumeFromLastSearchedColorOnStartupEnabled: Boolean,
    val changeResumeFromLastSearchedColorOnStartupEnablement: (Boolean) -> Unit,
    val isSmartBackspaceEnabled: Boolean,
    val changeSmartBackspaceEnablement: (Boolean) -> Unit,
    val isSelectAllTextOnTextFieldFocusEnabled: Boolean,
    val changeSelectAllTextOnTextFieldFocusEnablement: (Boolean) -> Unit,
)