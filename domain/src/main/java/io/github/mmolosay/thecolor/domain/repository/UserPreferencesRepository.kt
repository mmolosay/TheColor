package io.github.mmolosay.thecolor.domain.repository

import io.github.mmolosay.thecolor.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    fun flowOfColorInputType(): Flow<UserPreferences.ColorInputType>
    suspend fun setColorInputType(value: UserPreferences.ColorInputType?)

    fun flowOfAppUiThemeMode(): Flow<UserPreferences.UiThemeMode>
    suspend fun setAppUiThemeMode(value: UserPreferences.UiThemeMode?)
}