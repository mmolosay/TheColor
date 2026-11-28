package io.github.mmolosay.thecolor.presentation.settings.viewmodel

import kotlinx.collections.immutable.ImmutableList
import io.github.mmolosay.thecolor.domain.color.ColorInputType as DomainColorInputType
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.UiColorSchemeSet as DomainUiColorSchemeSet

data class SettingsViewData(
    val preferredColorInputType: DomainColorInputType,
    val appUiColorSchemeSet: DomainUiColorSchemeSet,
    val supportedAppUiColorSchemeSets: ImmutableList<DomainUiColorSchemeSet>,
    val isDynamicUiColorsEnabled: Boolean,
    val isResumeFromLastSearchedColorEnabled: Boolean,
    val isSmartBackspaceEnabled: Boolean,
    val isSelectAllTextOnTextFieldFocusEnabled: Boolean,
    val isAutoProceedWithRandomizedColorsEnabled: Boolean,
    val isDevOptionsEnabled: Boolean,
)