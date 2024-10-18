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

    override fun flowOfAppUiColorSchemeMode(): Flow<UserPreferences.UiColorSchemeMode> {
        fun defaultValue() = DefaultUserPreferences.AppUiColorSchemeMode
        val preferencesFlow = dataStore.data
        return preferencesFlow.map { preferences ->
            val modeClassDtoValue = preferences[DataStoreKeys.AppUiColorSchemeModeClass]
            val domainModeClass = if (modeClassDtoValue != null) {
                with(UiColorSchemeModeClassMapper) { modeClassDtoValue.toUiColorSchemeModeClass() }
            } else {
                return@map defaultValue()
            }
            when (domainModeClass) {
                UserPreferences.UiColorSchemeMode.Single::class -> {
                    val colorSchemeDtoValue = preferences[DataStoreKeys.AppUiColorSchemeSingle]
                    if (colorSchemeDtoValue != null) {
                        val domainColorScheme = with(UiColorSchemeMapper) { colorSchemeDtoValue.toUiColorScheme() }
                        UserPreferences.UiColorSchemeMode.Single(domainColorScheme)
                    } else {
                        return@map defaultValue()
                    }
                }
                UserPreferences.UiColorSchemeMode.Dual::class -> {
                    val lightColorSchemeDtoValue = preferences[DataStoreKeys.AppUiColorSchemeDualLight]
                        ?: return@map defaultValue()
                    val darkColorSchemeDtoValue = preferences[DataStoreKeys.AppUiColorSchemeDualDark]
                        ?: return@map defaultValue()
                    val domainLightColorScheme = with(UiColorSchemeMapper) { lightColorSchemeDtoValue.toUiColorScheme() }
                    val domainDarkColorScheme = with(UiColorSchemeMapper) { darkColorSchemeDtoValue.toUiColorScheme() }
                    UserPreferences.UiColorSchemeMode.Dual(
                        light = domainLightColorScheme,
                        dark = domainDarkColorScheme,
                    )
                }
                else -> defaultValue()
            }
        }
    }

    override suspend fun setAppUiColorSchemeMode(value: UserPreferences.UiColorSchemeMode?) {
        withContext(ioDispatcher) {
            dataStore.edit { preferences ->
                // clear all values beforehand to only have currently active mode to be not null
                preferences.remove(DataStoreKeys.AppUiColorSchemeModeClass)
                preferences.remove(DataStoreKeys.AppUiColorSchemeSingle)
                preferences.remove(DataStoreKeys.AppUiColorSchemeDualLight)
                preferences.remove(DataStoreKeys.AppUiColorSchemeDualDark)

                if (value == null) return@edit
                val modeClassDtoValue = with(UiColorSchemeModeClassMapper) { value.toDtoString() }
                preferences[DataStoreKeys.AppUiColorSchemeModeClass] = modeClassDtoValue
                when (value) {
                    is UserPreferences.UiColorSchemeMode.Single -> {
                        val colorSchemeDtoValue = with(UiColorSchemeMapper) { value.theme.toDtoString() }
                        preferences[DataStoreKeys.AppUiColorSchemeSingle] = colorSchemeDtoValue
                    }
                    is UserPreferences.UiColorSchemeMode.Dual -> {
                        val lightColorSchemeDtoValue = with(UiColorSchemeMapper) { value.light.toDtoString() }
                        val darkColorSchemeDtoValue = with(UiColorSchemeMapper) { value.dark.toDtoString() }
                        preferences[DataStoreKeys.AppUiColorSchemeDualLight] = lightColorSchemeDtoValue
                        preferences[DataStoreKeys.AppUiColorSchemeDualDark] = darkColorSchemeDtoValue
                    }
                }
            }
        }
    }

    private object DataStoreKeys {
        val ColorInputType = stringPreferencesKey("color_input_type")

        // TODO: consider using Proto DataStore for such complex classes
        /** Key for a class of a [UserPreferences.UiColorSchemeMode]. */
        val AppUiColorSchemeModeClass = stringPreferencesKey("app_ui_color_scheme_mode_class")

        /** Key for a [UserPreferences.UiColorScheme] if the mode is [UserPreferences.UiColorSchemeMode.Single]. */
        val AppUiColorSchemeSingle = stringPreferencesKey("app_ui_color_scheme_mode_single_value")

        /** Key for a light [UserPreferences.UiColorScheme] if the mode is [UserPreferences.UiColorSchemeMode.Dual]. */
        val AppUiColorSchemeDualLight = stringPreferencesKey("app_ui_color_scheme_mode_dual_light_value")

        /** Key for a dark [UserPreferences.UiColorScheme] if the mode is [UserPreferences.UiColorSchemeMode.Dual]. */
        val AppUiColorSchemeDualDark = stringPreferencesKey("app_ui_color_scheme_mode_dual_dark_value")
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

    fun String.toColorInputType(): UserPreferences.ColorInputType = // TODO: use direct import
        valueToDtoStringMap.entries
            .first { entry -> entry.value == this }
            .key

    fun UserPreferences.ColorInputType.toDtoString(): String =
        valueToDtoStringMap.getValue(this)
}

/**
 * Maps [UserPreferences.UiColorScheme] of domain layer to its representation in data layer (DTO)
 * and vice versa.
 */
private object UiColorSchemeMapper {

    private val valueToDtoStringMap = mapOf(
        UserPreferences.UiColorScheme.Light to "light",
        UserPreferences.UiColorScheme.Dark to "dark",
    )

    init {
        val registeredTypes = valueToDtoStringMap.keys
        val allTypes = UserPreferences.UiColorScheme.entries.toSet()
        check(registeredTypes == allTypes) { "You forgot to register new type" }
    }

    fun String.toUiColorScheme(): UserPreferences.UiColorScheme =
        valueToDtoStringMap.entries
            .first { entry -> entry.value == this }
            .key

    fun UserPreferences.UiColorScheme.toDtoString(): String =
        valueToDtoStringMap.getValue(this)
}

/**
 * Maps __class__ of [UserPreferences.UiColorSchemeMode] of domain layer to its representation in data layer (DTO)
 * and vice versa.
 */
private object UiColorSchemeModeClassMapper {

    private val valueToDtoStringMap = mapOf(
        UserPreferences.UiColorSchemeMode.Single::class to "single",
        UserPreferences.UiColorSchemeMode.Dual::class to "dual",
    )

    fun String.toUiColorSchemeModeClass(): KClass<out UserPreferences.UiColorSchemeMode> =
        valueToDtoStringMap.entries
            .first { entry -> entry.value == this }
            .key

    fun UserPreferences.UiColorSchemeMode.toDtoString(): String =
        valueToDtoStringMap.getValue(this::class)
}