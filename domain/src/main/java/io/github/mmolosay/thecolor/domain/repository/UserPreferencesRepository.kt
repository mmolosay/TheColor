package io.github.mmolosay.thecolor.domain.repository

import io.github.mmolosay.thecolor.domain.model.UserPreferences.ColorInputType
import io.github.mmolosay.thecolor.domain.model.UserPreferences.UiColorSchemeMode
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    fun flowOfColorInputType(): Flow<ColorInputType>
    suspend fun setColorInputType(value: ColorInputType?)

    fun flowOfAppUiColorSchemeMode(): Flow<UiColorSchemeMode>
    suspend fun setAppUiColorSchemeMode(value: UiColorSchemeMode?)
}