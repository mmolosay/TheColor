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
import kotlin.reflect.KClass

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

    override fun flowOfAppUiThemeMode(): Flow<UserPreferences.UiThemeMode> {
        fun defaultValue() = DefaultUserPreferences.AppUiThemeMode
        val preferencesFlow = dataStore.data
        return preferencesFlow.map { preferences ->
            val modeClassDtoValue = preferences[DataStoreKeys.AppUiThemeModeClass]
            val domainModeClass = if (modeClassDtoValue != null) {
                with(UiThemeModeClassMapper) { modeClassDtoValue.toUiThemeModeClass() }
            } else {
                return@map defaultValue()
            }
            when (domainModeClass) {
                UserPreferences.UiThemeMode.Single::class -> {
                    val themeDtoValue = preferences[DataStoreKeys.AppUiThemeSingle]
                    if (themeDtoValue != null) {
                        val domainTheme = with(UiThemeMapper) { themeDtoValue.toUiTheme() }
                        UserPreferences.UiThemeMode.Single(domainTheme)
                    } else {
                        return@map defaultValue()
                    }
                }
                UserPreferences.UiThemeMode.Dual::class -> {
                    val lightThemeDtoValue = preferences[DataStoreKeys.AppUiThemeDualLight]
                        ?: return@map defaultValue()
                    val darkThemeDtoValue = preferences[DataStoreKeys.AppUiThemeDualDark]
                        ?: return@map defaultValue()
                    val domainLightTheme = with(UiThemeMapper) { lightThemeDtoValue.toUiTheme() }
                    val domainDarkTheme = with(UiThemeMapper) { darkThemeDtoValue.toUiTheme() }
                    UserPreferences.UiThemeMode.Dual(
                        light = domainLightTheme,
                        dark = domainDarkTheme,
                    )
                }
                else -> defaultValue()
            }
        }
    }

    override suspend fun setAppUiThemeMode(value: UserPreferences.UiThemeMode?) {
        withContext(ioDispatcher) {
            dataStore.edit { preferences ->
                // clear all values beforehand to only have currently active mode to be not null
                preferences.remove(DataStoreKeys.AppUiThemeModeClass)
                preferences.remove(DataStoreKeys.AppUiThemeSingle)
                preferences.remove(DataStoreKeys.AppUiThemeDualLight)
                preferences.remove(DataStoreKeys.AppUiThemeDualDark)

                if (value == null) return@edit
                val modeClassDtoValue = with(UiThemeModeClassMapper) { value.toDtoString() }
                preferences[DataStoreKeys.AppUiThemeModeClass] = modeClassDtoValue
                when (value) {
                    is UserPreferences.UiThemeMode.Single -> {
                        val themeDtoValue = with(UiThemeMapper) { value.theme.toDtoString() }
                        preferences[DataStoreKeys.AppUiThemeSingle] = themeDtoValue
                    }
                    is UserPreferences.UiThemeMode.Dual -> {
                        val lightThemeDtoValue = with(UiThemeMapper) { value.light.toDtoString() }
                        val darkThemeDtoValue = with(UiThemeMapper) { value.dark.toDtoString() }
                        preferences[DataStoreKeys.AppUiThemeDualLight] = lightThemeDtoValue
                        preferences[DataStoreKeys.AppUiThemeDualDark] = darkThemeDtoValue
                    }
                }
            }
        }
    }

    private object DataStoreKeys {
        val ColorInputType = stringPreferencesKey("color_input_type")

        // TODO: consider using Proto DataStore for such complex classes
        val AppUiThemeModeClass = stringPreferencesKey("app_ui_theme_mode_class")
        val AppUiThemeSingle = stringPreferencesKey("app_ui_theme_single")
        val AppUiThemeDualLight = stringPreferencesKey("app_ui_theme_dual_light")
        val AppUiThemeDualDark = stringPreferencesKey("app_ui_theme_dual_dark")
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

/**
 * Maps class of [UserPreferences.UiThemeMode] of domain layer to its representation in data layer (DTO)
 * and vice versa.
 */
private object UiThemeModeClassMapper {

    private val valueToDtoStringMap = mapOf(
        UserPreferences.UiThemeMode.Single::class to "single",
        UserPreferences.UiThemeMode.Dual::class to "dual",
    )

    fun String.toUiThemeModeClass(): KClass<out UserPreferences.UiThemeMode> =
        valueToDtoStringMap.entries
            .first { entry -> entry.value == this }
            .key

    fun UserPreferences.UiThemeMode.toDtoString(): String =
        valueToDtoStringMap.getValue(this::class)
}