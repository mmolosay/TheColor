package io.github.mmolosay.thecolor.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.mmolosay.thecolor.domain.model.UserPreferences
import io.github.mmolosay.thecolor.domain.repository.DefaultUserPreferences
import io.github.mmolosay.thecolor.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named

/**
 * Implementation of [UserPreferencesRepository] powered by DataStore library.
 */
class UserPreferencesDataStoreRepository @Inject constructor(
    @Named("UserPreferences") private val dataStore: DataStore<Preferences>,
    @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher,
) : UserPreferencesRepository {

    override fun flowOfColorInputType(): Flow<UserPreferences.ColorInputType> {
        val preferencesFlow = dataStore.data
        return preferencesFlow.map { preferences ->
            val dtoValue = preferences[DataStoreKeys.ColorInputType]
            val domainModel = if (dtoValue != null) {
                with(ColorInputTypeMapper) { dtoValue.toColorInputType() }
            } else {
                DefaultUserPreferences.ColorInputType
            }
            domainModel
        }
    }

    override suspend fun setColorInputType(value: UserPreferences.ColorInputType?) {
        withContext(ioDispatcher) {
            val dtoValue = with(ColorInputTypeMapper) { value?.toDtoString() }
            dataStore.edit { preferences ->
                if (dtoValue != null) {
                    preferences[DataStoreKeys.ColorInputType] = dtoValue
                } else {
                    preferences.remove(DataStoreKeys.ColorInputType)
                }
            }
        }
    }

    override fun flowOfAppUiTheme(): Flow<UserPreferences.UiTheme> {
        val preferencesFlow = dataStore.data
        return preferencesFlow.map { preferences ->
            val dtoValue = preferences[DataStoreKeys.AppUiTheme]
            val domainModel = if (dtoValue != null) {
                with(UiThemeMapper) { dtoValue.toUiTheme() }
            } else {
                DefaultUserPreferences.AppUiTheme
            }
            domainModel
        }
    }

    override suspend fun setAppUiTheme(value: UserPreferences.UiTheme?) {
        withContext(ioDispatcher) {
            val dtoValue = with(UiThemeMapper) { value?.toDtoString() }
            dataStore.edit { preferences ->
                if (dtoValue != null) {
                    preferences[DataStoreKeys.AppUiTheme] = dtoValue
                } else {
                    preferences.remove(DataStoreKeys.AppUiTheme)
                }
            }
        }
    }

    private object DataStoreKeys {
        val ColorInputType = stringPreferencesKey("color_input_type")
        val AppUiTheme = stringPreferencesKey("app_ui_theme")
    }
}

/**
 * Maps [UserPreferences.ColorInputType] of domain layer to its representation in data layer (DTO)
 * and vice versa.
 */
private object ColorInputTypeMapper {

    private val valueToDtoStringMap = mapOf(
        UserPreferences.ColorInputType.Hex to "hex",
        UserPreferences.ColorInputType.Rgb to "rgb",
    )

    init {
        val registeredTypes = valueToDtoStringMap.keys
        val allTypes = UserPreferences.ColorInputType.entries.toSet()
        check(registeredTypes == allTypes) { "You forgot to register new type" }
    }

    fun String.toColorInputType(): UserPreferences.ColorInputType =
        valueToDtoStringMap.entries
            .first { entry -> entry.value == this }
            .key

    fun UserPreferences.ColorInputType.toDtoString(): String =
        valueToDtoStringMap.getValue(this)
}

/**
 * Maps [UserPreferences.UiTheme] of domain layer to its representation in data layer (DTO)
 * and vice versa.
 */
private object UiThemeMapper {

    private val valueToDtoStringMap = mapOf(
        UserPreferences.UiTheme.Light to "light",
        UserPreferences.UiTheme.Dark to "dark",
        UserPreferences.UiTheme.FollowsSystem to "follows_system",
    )

    init {
        val registeredTypes = valueToDtoStringMap.keys
        val allTypes = UserPreferences.UiTheme.entries.toSet()
        check(registeredTypes == allTypes) { "You forgot to register new type" }
    }

    fun String.toUiTheme(): UserPreferences.UiTheme =
        valueToDtoStringMap.entries
            .first { entry -> entry.value == this }
            .key

    fun UserPreferences.UiTheme.toDtoString(): String =
        valueToDtoStringMap.getValue(this)
}