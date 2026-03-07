package io.github.mmolosay.thecolor.domain.user.preferences

import io.github.mmolosay.thecolor.domain.color.ColorInputType
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.AutoProceedWithRandomizedColors
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.DynamicUiColors
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.ResumeFromLastSearchedColorOnStartup
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.SelectAllTextOnTextFieldFocus
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.SmartBackspace
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.UiColorSchemeSet
import kotlinx.coroutines.flow.StateFlow

/**
 * All Flows have nullable types.
 * If a particular flow emits null, that means that this flow is only being initialized yet.
 * If a particular flow emits not-null value, that means that either
 * a: this exact value is stored, or
 * b: there is no value stored for this feature, and the returned value is a default one.
 */
interface UserPreferencesRepository {
    val flowOfColorInputType: StateFlow<ColorInputType?>
    suspend fun setColorInputType(value: ColorInputType?)

    val flowOfAppUiColorSchemeSet: StateFlow<UiColorSchemeSet?>
    suspend fun setAppUiColorSchemeSet(value: UiColorSchemeSet?)

    val flowOfDynamicUiColors: StateFlow<DynamicUiColors?>
    suspend fun setDynamicUiColors(value: DynamicUiColors?)

    val flowOfResumeFromLastSearchedColorOnStartup: StateFlow<ResumeFromLastSearchedColorOnStartup?>
    suspend fun setResumeFromLastSearchedColorOnStartup(value: ResumeFromLastSearchedColorOnStartup?)

    val flowOfSmartBackspace: StateFlow<SmartBackspace?>
    suspend fun setSmartBackspace(value: SmartBackspace?)

    val flowOfSelectAllTextOnTextFieldFocus: StateFlow<SelectAllTextOnTextFieldFocus?>
    suspend fun setSelectAllTextOnTextFieldFocus(value: SelectAllTextOnTextFieldFocus?)

    val flowOfAutoProceedWithRandomizedColors: StateFlow<AutoProceedWithRandomizedColors?>
    suspend fun setAutoProceedWithRandomizedColors(value: AutoProceedWithRandomizedColors?)
}