package io.github.mmolosay.thecolor.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.mmolosay.thecolor.domain.model.ColorInputType
import io.github.mmolosay.thecolor.domain.model.UserPreferences
import io.github.mmolosay.thecolor.domain.model.UserPreferences.ResumeFromLastSearchedColorOnStartup
import io.github.mmolosay.thecolor.domain.model.UserPreferences.SelectAllTextOnTextFieldFocus
import io.github.mmolosay.thecolor.domain.model.UserPreferences.SmartBackspace
import io.github.mmolosay.thecolor.domain.model.UserPreferences.UiColorScheme
import io.github.mmolosay.thecolor.domain.model.UserPreferences.UiColorSchemeSet
import io.github.mmolosay.thecolor.domain.repository.DefaultUserPreferences
import io.github.mmolosay.thecolor.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Implementation of [UserPreferencesRepository] powered by DataStore library.
 */
@Singleton
class UserPreferencesDataStoreRepository @Inject constructor(
    @Named("UserPreferences") private val dataStore: DataStore<Preferences>,
    @Named("ApplicationScope") private val appScope: CoroutineScope,
    @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher,
) : UserPreferencesRepository {

    private val stateFlowOfColorInputType: StateFlow<ColorInputType?> =
        dataStore.data
            .map { it.getColorInputType() }
            .stateEagerlyInAppScope()

    private val stateFlowOfAppUiColorSchemeSet: StateFlow<UiColorSchemeSet?> =
        dataStore.data
            .map { it.getAppUiColorSchemeSet() }
            .stateEagerlyInAppScope()

    private val stateFlowOfResumeFromLastSearchedColorOnStartup: StateFlow<ResumeFromLastSearchedColorOnStartup?> =
        dataStore.data
            .map { it.getResumeFromLastSearchedColorOnStartup() }
            .stateEagerlyInAppScope()

    private val stateFlowOfSmartBackspace: StateFlow<SmartBackspace?> =
        dataStore.data
            .map { it.getSmartBackspace() }
            .stateEagerlyInAppScope()

    private val stateFlowOfSelectAllTextOnTextFieldFocus: StateFlow<SelectAllTextOnTextFieldFocus?> =
        dataStore.data
            .map { it.getSelectAllTextOnTextFieldFocus() }
            .stateEagerlyInAppScope()

    override fun flowOfColorInputType(): Flow<ColorInputType> =
        stateFlowOfColorInputType.filterNotNull()

    private fun Preferences.getColorInputType(): ColorInputType {
        val dtoValue = this[DataStoreKeys.ColorInputType]
        return if (dtoValue != null) {
            with(ColorInputTypeMapper) { dtoValue.toColorInputType() }
        } else {
            DefaultUserPreferences.PreferredColorInputType
        }
    }

    override suspend fun setColorInputType(value: ColorInputType?) {
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

    override fun flowOfAppUiColorSchemeSet(): Flow<UiColorSchemeSet> =
        stateFlowOfAppUiColorSchemeSet.filterNotNull()

    private fun Preferences.getAppUiColorSchemeSet(): UiColorSchemeSet {
        fun defaultValue() = DefaultUserPreferences.AppUiColorSchemeSet
        val domainLightColorScheme = kotlin.run {
            val lightColorSchemeDtoValue = this[DataStoreKeys.AppUiColorSchemeLight]
                ?: return defaultValue()
            with(UiColorSchemeMapper) { lightColorSchemeDtoValue.toUiColorScheme() }
        }
        val domainDarkColorScheme = kotlin.run {
            val darkColorSchemeDtoValue = this[DataStoreKeys.AppUiColorSchemeDark]
                ?: return defaultValue()
            with(UiColorSchemeMapper) { darkColorSchemeDtoValue.toUiColorScheme() }
        }
        return UiColorSchemeSet(
            light = domainLightColorScheme,
            dark = domainDarkColorScheme,
        )
    }

    override suspend fun setAppUiColorSchemeSet(value: UiColorSchemeSet?) {
        withContext(ioDispatcher) {
            dataStore.edit { preferences ->
                if (value == null) {
                    preferences.remove(DataStoreKeys.AppUiColorSchemeLight)
                    preferences.remove(DataStoreKeys.AppUiColorSchemeDark)
                    return@edit
                }

                kotlin.run {
                    val lightColorSchemeDtoValue =
                        with(UiColorSchemeMapper) { value.light.toDtoString() }
                    preferences[DataStoreKeys.AppUiColorSchemeLight] = lightColorSchemeDtoValue
                }
                kotlin.run {
                    val darkColorSchemeDtoValue =
                        with(UiColorSchemeMapper) { value.dark.toDtoString() }
                    preferences[DataStoreKeys.AppUiColorSchemeDark] = darkColorSchemeDtoValue
                }
            }
        }
    }

    override fun flowOfResumeFromLastSearchedColorOnStartup(): Flow<ResumeFromLastSearchedColorOnStartup> =
        stateFlowOfResumeFromLastSearchedColorOnStartup.filterNotNull()

    private fun Preferences.getResumeFromLastSearchedColorOnStartup(): ResumeFromLastSearchedColorOnStartup {
        val dtoValue = this[DataStoreKeys.ShouldResumeFromLastSearchedColorOnStartup]
        return if (dtoValue != null) {
            ResumeFromLastSearchedColorOnStartup(
                enabled = dtoValue, // boolean stays boolean in both Data and Domain layers
            )
        } else {
            DefaultUserPreferences.ResumeFromLastSearchedColorOnStartup
        }
    }

    override suspend fun setResumeFromLastSearchedColorOnStartup(value: ResumeFromLastSearchedColorOnStartup?) {
        withContext(ioDispatcher) {
            dataStore.edit { preferences ->
                if (value != null) {
                    preferences[DataStoreKeys.ShouldResumeFromLastSearchedColorOnStartup] =
                        value.enabled
                } else {
                    preferences.remove(DataStoreKeys.ShouldResumeFromLastSearchedColorOnStartup)
                }
            }
        }
    }

    override fun flowOfSmartBackspace(): Flow<SmartBackspace> =
        stateFlowOfSmartBackspace.filterNotNull()

    private fun Preferences.getSmartBackspace(): SmartBackspace {
        val dtoValue = this[DataStoreKeys.SmartBackspace]
        return if (dtoValue != null) {
            SmartBackspace(
                enabled = dtoValue, // boolean stays boolean in both Data and Domain layers
            )
        } else {
            DefaultUserPreferences.SmartBackspace
        }
    }

    override suspend fun setSmartBackspace(value: SmartBackspace?) {
        withContext(ioDispatcher) {
            dataStore.edit { preferences ->
                if (value != null) {
                    preferences[DataStoreKeys.SmartBackspace] = value.enabled
                } else {
                    preferences.remove(DataStoreKeys.SmartBackspace)
                }
            }
        }
    }

    override fun flowOfSelectAllTextOnTextFieldFocus(): Flow<SelectAllTextOnTextFieldFocus> =
        stateFlowOfSelectAllTextOnTextFieldFocus.filterNotNull()

    private fun Preferences.getSelectAllTextOnTextFieldFocus(): SelectAllTextOnTextFieldFocus {
        val dtoValue = this[DataStoreKeys.SelectAllTextOnTextFieldFocus]
        return if (dtoValue != null) {
            SelectAllTextOnTextFieldFocus(
                enabled = dtoValue, // boolean stays boolean in both Data and Domain layers
            )
        } else {
            DefaultUserPreferences.SelectAllTextOnTextFieldFocus
        }
    }

    override suspend fun setSelectAllTextOnTextFieldFocus(value: SelectAllTextOnTextFieldFocus?) {
        withContext(ioDispatcher) {
            dataStore.edit { preferences ->
                if (value != null) {
                    preferences[DataStoreKeys.SelectAllTextOnTextFieldFocus] = value.enabled
                } else {
                    preferences.remove(DataStoreKeys.SelectAllTextOnTextFieldFocus)
                }
            }
        }
    }

    private fun <T> Flow<T>.stateEagerlyInAppScope(): StateFlow<T?> =
        this.stateIn(
            scope = appScope,
            started = SharingStarted.Eagerly, // will access DB immediately when class is created, 
            // so that values are ready before first collection
            initialValue = null,
        )

    private object DataStoreKeys {
        val ColorInputType = stringPreferencesKey("color_input_type")

        // TODO: consider using Proto DataStore for such complex classes
        /** Key for a `light` [UserPreferences.UiColorScheme] from the [UserPreferences.UiColorSchemeSet]. */
        val AppUiColorSchemeLight = stringPreferencesKey("app_ui_color_scheme_set_light_value")

        /** Key for a `dark` [UserPreferences.UiColorScheme] from the [UserPreferences.UiColorSchemeSet]. */
        val AppUiColorSchemeDark = stringPreferencesKey("app_ui_color_scheme_set_dark_value")

        val ShouldResumeFromLastSearchedColorOnStartup =
            booleanPreferencesKey("should_resume_from_last_searched_color_on_startup")

        val SmartBackspace = booleanPreferencesKey("smart_backspace")

        val SelectAllTextOnTextFieldFocus =
            booleanPreferencesKey("select_all_text_on_text_field_focus")
    }
}

/**
 * Maps [ColorInputType] of domain layer to its representation in data layer (DTO)
 * and vice versa.
 */
private object ColorInputTypeMapper {

    private val valueToDtoStringMap = mapOf(
        ColorInputType.Hex to "hex",
        ColorInputType.Rgb to "rgb",
    )

    init {
        val registeredTypes = valueToDtoStringMap.keys
        val allTypes = ColorInputType.entries.toSet()
        check(registeredTypes == allTypes) { "You forgot to register new type" }
    }

    fun String.toColorInputType(): ColorInputType =
        valueToDtoStringMap.entries
            .first { entry -> entry.value == this }
            .key

    fun ColorInputType.toDtoString(): String =
        valueToDtoStringMap.getValue(this)
}

/**
 * Maps [UserPreferences.UiColorScheme] of domain layer to its representation in data layer (DTO)
 * and vice versa.
 */
private object UiColorSchemeMapper {

    private val valueToDtoStringMap = mapOf(
        UiColorScheme.Light to "light",
        UiColorScheme.Dark to "dark",
    )

    init {
        val registeredTypes = valueToDtoStringMap.keys
        val allTypes = UiColorScheme.entries.toSet()
        check(registeredTypes == allTypes) { "You forgot to register new type" }
    }

    fun String.toUiColorScheme(): UiColorScheme =
        valueToDtoStringMap.entries
            .first { entry -> entry.value == this }
            .key

    fun UiColorScheme.toDtoString(): String =
        valueToDtoStringMap.getValue(this)
}