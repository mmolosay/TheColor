package io.github.mmolosay.thecolor.domain.repository

import io.github.mmolosay.thecolor.domain.model.ColorInputType
import io.github.mmolosay.thecolor.domain.model.UserPreferences.AutoProceedWithRandomizedColors
import io.github.mmolosay.thecolor.domain.model.UserPreferences.DynamicUiColors
import io.github.mmolosay.thecolor.domain.model.UserPreferences.ResumeFromLastSearchedColorOnStartup
import io.github.mmolosay.thecolor.domain.model.UserPreferences.SelectAllTextOnTextFieldFocus
import io.github.mmolosay.thecolor.domain.model.UserPreferences.SmartBackspace
import io.github.mmolosay.thecolor.domain.model.UserPreferences.UiColorSchemeSet
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val flowOfColorInputType: Flow<ColorInputType>
    suspend fun setColorInputType(value: ColorInputType?)

    val flowOfAppUiColorSchemeSet: Flow<UiColorSchemeSet>
    suspend fun setAppUiColorSchemeSet(value: UiColorSchemeSet?)

    val flowOfDynamicUiColors: Flow<DynamicUiColors>
    suspend fun setDynamicUiColors(value: DynamicUiColors?)

    val flowOfResumeFromLastSearchedColorOnStartup: Flow<ResumeFromLastSearchedColorOnStartup>
    suspend fun setResumeFromLastSearchedColorOnStartup(value: ResumeFromLastSearchedColorOnStartup?)

    val flowOfSmartBackspace: Flow<SmartBackspace>
    suspend fun setSmartBackspace(value: SmartBackspace?)

    val flowOfSelectAllTextOnTextFieldFocus: Flow<SelectAllTextOnTextFieldFocus>
    suspend fun setSelectAllTextOnTextFieldFocus(value: SelectAllTextOnTextFieldFocus?)

    val flowOfAutoProceedWithRandomizedColors: Flow<AutoProceedWithRandomizedColors>
    suspend fun setAutoProceedWithRandomizedColors(value: AutoProceedWithRandomizedColors?)
}