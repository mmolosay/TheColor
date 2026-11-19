package io.github.mmolosay.thecolor.presentation.settings

import io.github.mmolosay.thecolor.domain.color.ColorInputType as DomainColorInputType
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.AutoProceedWithRandomizedColors as DomainAutoProceedWithRandomizedColors
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.DynamicUiColors as DomainDynamicUiColors
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.ResumeFromLastSearchedColorOnStartup as DomainResumeFromLastSearchedColorOnStartup
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.SelectAllTextOnTextFieldFocus as DomainSelectAllTextOnTextFieldFocus
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.SmartBackspace as DomainSmartBackspace
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.UiColorSchemeSet as DomainUiColorSchemeSet

data class SettingsState(
    val preferredColorInputType: DomainColorInputType?,
    val appUiColorSchemeSet: DomainUiColorSchemeSet?,
    val dynamicUiColors: DomainDynamicUiColors?,
    val resumeFromLastSearchedColor: DomainResumeFromLastSearchedColorOnStartup?,
    val smartBackspace: DomainSmartBackspace?,
    val selectAllTextOnTextFieldFocus: DomainSelectAllTextOnTextFieldFocus?,
    val autoProceedWithRandomizedColors: DomainAutoProceedWithRandomizedColors?,
)