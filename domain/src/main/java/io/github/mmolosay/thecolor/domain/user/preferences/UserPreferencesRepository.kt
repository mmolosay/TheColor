package io.github.mmolosay.thecolor.domain.user.preferences

import io.github.mmolosay.thecolor.domain.color.ColorInputType
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.AutoProceedWithRandomizedColors
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.DynamicUiColors
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.ResumeFromLastSearchedColorOnStartup
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.SelectAllTextOnTextFieldFocus
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.SmartBackspace
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.UiColorSchemeSet
import io.github.mmolosay.thecolor.utils.DataState
import kotlinx.coroutines.flow.StateFlow

interface UserPreferencesRepository {
    val flowOfColorInputType: StateFlow<DataState<ColorInputType>>
    suspend fun setColorInputType(value: ColorInputType?)

    val flowOfAppUiColorSchemeSet: StateFlow<DataState<UiColorSchemeSet>>
    suspend fun setAppUiColorSchemeSet(value: UiColorSchemeSet?)

    val flowOfDynamicUiColors: StateFlow<DataState<DynamicUiColors>>
    suspend fun setDynamicUiColors(value: DynamicUiColors?)

    val flowOfResumeFromLastSearchedColorOnStartup: StateFlow<DataState<ResumeFromLastSearchedColorOnStartup>>
    suspend fun setResumeFromLastSearchedColorOnStartup(value: ResumeFromLastSearchedColorOnStartup?)

    val flowOfSmartBackspace: StateFlow<DataState<SmartBackspace>>
    suspend fun setSmartBackspace(value: SmartBackspace?)

    val flowOfSelectAllTextOnTextFieldFocus: StateFlow<DataState<SelectAllTextOnTextFieldFocus>>
    suspend fun setSelectAllTextOnTextFieldFocus(value: SelectAllTextOnTextFieldFocus?)

    val flowOfAutoProceedWithRandomizedColors: StateFlow<DataState<AutoProceedWithRandomizedColors>>
    suspend fun setAutoProceedWithRandomizedColors(value: AutoProceedWithRandomizedColors?)

    suspend fun clear()
}