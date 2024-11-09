package io.github.mmolosay.thecolor.domain.repository

import io.github.mmolosay.thecolor.domain.model.ColorInputType
import io.github.mmolosay.thecolor.domain.model.UserPreferences.ShouldResumeFromLastSearchedColorOnStartup
import io.github.mmolosay.thecolor.domain.model.UserPreferences.SmartBackspace
import io.github.mmolosay.thecolor.domain.model.UserPreferences.UiColorSchemeSet
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    fun flowOfColorInputType(): Flow<ColorInputType>
    suspend fun setColorInputType(value: ColorInputType?)

    fun flowOfAppUiColorSchemeSet(): Flow<UiColorSchemeSet>
    suspend fun setAppUiColorSchemeSet(value: UiColorSchemeSet?)

    fun flowOfShouldResumeFromLastSearchedColorOnStartup(): Flow<ShouldResumeFromLastSearchedColorOnStartup>
    suspend fun setShouldResumeFromLastSearchedColorOnStartup(value: ShouldResumeFromLastSearchedColorOnStartup?)

    fun flowOfSmartBackspace(): Flow<SmartBackspace>
    suspend fun setSmartBackspace(value: SmartBackspace?)
}