package io.github.mmolosay.thecolor.domain.repository

import io.github.mmolosay.thecolor.domain.model.ColorInputType
import io.github.mmolosay.thecolor.domain.model.UserPreferences
import io.github.mmolosay.thecolor.domain.model.UserPreferences.ResumeFromLastSearchedColorOnStartup
import io.github.mmolosay.thecolor.domain.model.UserPreferences.SelectAllTextOnTextFieldFocus
import io.github.mmolosay.thecolor.domain.model.UserPreferences.SmartBackspace
import io.github.mmolosay.thecolor.domain.model.UserPreferences.UiColorSchemeSet
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    fun flowOfColorInputType(): Flow<ColorInputType>
    suspend fun setColorInputType(value: ColorInputType?)

    fun flowOfAppUiColorSchemeSet(): Flow<UiColorSchemeSet>
    suspend fun setAppUiColorSchemeSet(value: UiColorSchemeSet?)

    fun flowOfResumeFromLastSearchedColorOnStartup(): Flow<ResumeFromLastSearchedColorOnStartup>
    suspend fun setResumeFromLastSearchedColorOnStartup(value: ResumeFromLastSearchedColorOnStartup?)

    fun flowOfSmartBackspace(): Flow<SmartBackspace>
    suspend fun setSmartBackspace(value: SmartBackspace?)

    fun flowOfSelectAllTextOnTextFieldFocus(): Flow<SelectAllTextOnTextFieldFocus>
    suspend fun setSelectAllTextOnTextFieldFocus(value: SelectAllTextOnTextFieldFocus?)
}