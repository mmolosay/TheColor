package io.github.mmolosay.thecolor.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.mmolosay.thecolor.data.local.utils.setOrRemoveValue
import io.github.mmolosay.thecolor.domain.color.ColorInputType
import io.github.mmolosay.thecolor.domain.user.preferences.IllegalStoredValueException
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.AutoProceedWithRandomizedColors
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.DynamicUiColors
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.ResumeFromLastSearchedColorOnStartup
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.SelectAllTextOnTextFieldFocus
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.SmartBackspace
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.UiColorScheme
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.UiColorSchemeSet
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferencesRepository
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferencesRepository.DataState
import io.github.mmolosay.thecolor.main.di.qualifiers.AppCoroutineScope
import io.github.mmolosay.thecolor.main.di.qualifiers.CoroutineDispatcherDiQualifiers.IoDispatcher
import io.github.mmolosay.thecolor.main.di.qualifiers.DataStoreDiQualifiers.UserPreferences
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [UserPreferencesRepository] powered by DataStore library.
 */
@Singleton
class UserPreferencesDataStoreRepository @Inject constructor(
    @UserPreferences private val dataStore: DataStore<Preferences>,
    @AppCoroutineScope private val appScope: CoroutineScope,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : UserPreferencesRepository {

    override val flowOfColorInputType: StateFlow<DataState<ColorInputType>> =
        dataStore.data
            .map { it.getColorInputType() }
            .stateEagerlyInAppScope(initialValue = DataState.BeingInitialized)

    private fun Preferences.getColorInputType(): DataState<ColorInputType> {
        val key = DataStoreKeys.ColorInputType
        if (key !in this) return DataState.NoValueStored
        val dtoValue = this[key]
        if (dtoValue != null) {
            val value = with(ColorInputTypeMapper) { dtoValue.toColorInputType() }
            return DataState.HasValueStored(value)
        } else {
            throw IllegalStoredValueException<ColorInputType>(value = dtoValue)
        }
    }

    override suspend fun setColorInputType(value: ColorInputType?) {
        withContext(ioDispatcher) {
            dataStore.setOrRemoveValue(
                key = DataStoreKeys.ColorInputType,
                value = with(ColorInputTypeMapper) { value?.toDtoString() },
            )
        }
    }

    override val flowOfAppUiColorSchemeSet: StateFlow<DataState<UiColorSchemeSet>> =
        dataStore.data
            .map { it.getAppUiColorSchemeSet() }
            .stateEagerlyInAppScope(initialValue = DataState.BeingInitialized)

    private fun Preferences.getAppUiColorSchemeSet(): DataState<UiColorSchemeSet> {
        fun Preferences.getAppUiColorScheme(key: Preferences.Key<String>): DataState<UiColorScheme> {
            if (key !in this) return DataState.NoValueStored
            val dtoValue = this[key]
            if (dtoValue != null) {
                val value = with(UiColorSchemeMapper) { dtoValue.toUiColorScheme() }
                return DataState.HasValueStored(value)
            } else {
                throw IllegalStoredValueException<UiColorSchemeSet>(value = dtoValue)
            }
        }
        // DS — DataState
        val lightDs = getAppUiColorScheme(key = DataStoreKeys.AppUiColorSchemeLight)
        val darkDs = getAppUiColorScheme(key = DataStoreKeys.AppUiColorSchemeDark)
        if (lightDs is DataState.HasValueStored && darkDs is DataState.HasValueStored) {
            val value = UiColorSchemeSet(light = lightDs.value, dark = darkDs.value)
            return DataState.HasValueStored(value)
        }
        if (lightDs is DataState.NoValueStored || darkDs is DataState.NoValueStored) {
            return DataState.NoValueStored
        }
        error("unexpected DataState")
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

    override val flowOfDynamicUiColors: StateFlow<DataState<DynamicUiColors>> =
        dataStore.data
            .map { it.getDynamicUiColors() }
            .stateEagerlyInAppScope(initialValue = DataState.BeingInitialized)

    private fun Preferences.getDynamicUiColors(): DataState<DynamicUiColors> {
        val key = DataStoreKeys.DynamicUiColors
        if (key !in this) return DataState.NoValueStored
        val dtoValue = this[key]
        if (dtoValue != null) {
            val value = DynamicUiColors(enabled = dtoValue) // boolean stays boolean in both Data and Domain layers
            return DataState.HasValueStored(value)
        } else {
            throw IllegalStoredValueException<DynamicUiColors>(value = dtoValue)
        }
    }

    override suspend fun setDynamicUiColors(value: DynamicUiColors?) {
        withContext(ioDispatcher) {
            dataStore.setOrRemoveValue(
                key = DataStoreKeys.DynamicUiColors,
                value = value?.enabled,
            )
        }
    }

    override val flowOfResumeFromLastSearchedColorOnStartup: StateFlow<DataState<ResumeFromLastSearchedColorOnStartup>> =
        dataStore.data
            .map { it.getResumeFromLastSearchedColorOnStartup() }
            .stateEagerlyInAppScope(initialValue = DataState.BeingInitialized)

    private fun Preferences.getResumeFromLastSearchedColorOnStartup(): DataState<ResumeFromLastSearchedColorOnStartup> {
        val key = DataStoreKeys.ShouldResumeFromLastSearchedColorOnStartup
        if (key !in this) return DataState.NoValueStored
        val dtoValue = this[key]
        if (dtoValue != null) {
            val value = ResumeFromLastSearchedColorOnStartup(enabled = dtoValue) // boolean stays boolean in both Data and Domain layers
            return DataState.HasValueStored(value)
        } else {
            throw IllegalStoredValueException<ResumeFromLastSearchedColorOnStartup>(value = dtoValue)
        }
    }

    override suspend fun setResumeFromLastSearchedColorOnStartup(value: ResumeFromLastSearchedColorOnStartup?) {
        withContext(ioDispatcher) {
            dataStore.setOrRemoveValue(
                key = DataStoreKeys.ShouldResumeFromLastSearchedColorOnStartup,
                value = value?.enabled,
            )
        }
    }

    override val flowOfSmartBackspace: StateFlow<DataState<SmartBackspace>> =
        dataStore.data
            .map { it.getSmartBackspace() }
            .stateEagerlyInAppScope(initialValue = DataState.BeingInitialized)

    private fun Preferences.getSmartBackspace(): DataState<SmartBackspace> {
        val key = DataStoreKeys.SmartBackspace
        if (key !in this) return DataState.NoValueStored
        val dtoValue = this[key]
        if (dtoValue != null) {
            val value = SmartBackspace(enabled = dtoValue) // boolean stays boolean in both Data and Domain layers
            return DataState.HasValueStored(value)
        } else {
            throw IllegalStoredValueException<SmartBackspace>(value = dtoValue)
        }
    }

    override suspend fun setSmartBackspace(value: SmartBackspace?) {
        withContext(ioDispatcher) {
            dataStore.setOrRemoveValue(
                key = DataStoreKeys.SmartBackspace,
                value = value?.enabled,
            )
        }
    }

    override val flowOfSelectAllTextOnTextFieldFocus: StateFlow<DataState<SelectAllTextOnTextFieldFocus>> =
        dataStore.data
            .map { it.getSelectAllTextOnTextFieldFocus() }
            .stateEagerlyInAppScope(initialValue = DataState.BeingInitialized)

    private fun Preferences.getSelectAllTextOnTextFieldFocus(): DataState<SelectAllTextOnTextFieldFocus> {
        val key = DataStoreKeys.SelectAllTextOnTextFieldFocus
        if (key !in this) return DataState.NoValueStored
        val dtoValue = this[key]
        if (dtoValue != null) {
            val value = SelectAllTextOnTextFieldFocus(enabled = dtoValue) // boolean stays boolean in both Data and Domain layers
            return DataState.HasValueStored(value)
        } else {
            throw IllegalStoredValueException<SelectAllTextOnTextFieldFocus>(value = dtoValue)
        }
    }

    override suspend fun setSelectAllTextOnTextFieldFocus(value: SelectAllTextOnTextFieldFocus?) {
        withContext(ioDispatcher) {
            dataStore.setOrRemoveValue(
                key = DataStoreKeys.SelectAllTextOnTextFieldFocus,
                value = value?.enabled,
            )
        }
    }

    override val flowOfAutoProceedWithRandomizedColors: StateFlow<DataState<AutoProceedWithRandomizedColors>> =
        dataStore.data
            .map { it.getAutoProceedWithRandomizedColors() }
            .stateEagerlyInAppScope(initialValue = DataState.BeingInitialized)

    private fun Preferences.getAutoProceedWithRandomizedColors(): DataState<AutoProceedWithRandomizedColors> {
        val key = DataStoreKeys.AutoProceedWithRandomizedColors
        if (key !in this) return DataState.NoValueStored
        val dtoValue = this[key]
        if (dtoValue != null) {
            val value = AutoProceedWithRandomizedColors(enabled = dtoValue) // boolean stays boolean in both Data and Domain layers
            return DataState.HasValueStored(value)
        } else {
            throw IllegalStoredValueException<AutoProceedWithRandomizedColors>(value = dtoValue)
        }
    }

    override suspend fun setAutoProceedWithRandomizedColors(value: AutoProceedWithRandomizedColors?) {
        withContext(ioDispatcher) {
            dataStore.setOrRemoveValue(
                value = value?.enabled,
                key = DataStoreKeys.AutoProceedWithRandomizedColors,
            )
        }
    }

    private fun <T> Flow<T>.stateEagerlyInAppScope(
        initialValue: T,
    ): StateFlow<T> =
        this.stateIn(
            scope = appScope,
            started = SharingStarted.Eagerly, // will access DB immediately when class is created,
            // so that values are ready before first collection
            initialValue = initialValue,
        )

    private object DataStoreKeys {
        val ColorInputType = stringPreferencesKey("color_input_type")

        /** Key for a `light` [UiColorScheme] from the [UiColorSchemeSet]. */
        val AppUiColorSchemeLight = stringPreferencesKey("app_ui_color_scheme_set_light_value")

        /** Key for a `dark` [UiColorScheme] from the [UiColorSchemeSet]. */
        val AppUiColorSchemeDark = stringPreferencesKey("app_ui_color_scheme_set_dark_value")

        val DynamicUiColors = booleanPreferencesKey("dynamic_ui_colors")

        val ShouldResumeFromLastSearchedColorOnStartup =
            booleanPreferencesKey("should_resume_from_last_searched_color_on_startup")

        val SmartBackspace = booleanPreferencesKey("smart_backspace")

        val SelectAllTextOnTextFieldFocus =
            booleanPreferencesKey("select_all_text_on_text_field_focus")

        val AutoProceedWithRandomizedColors =
            booleanPreferencesKey("auto_proceed_with_randomized_colors")
    }
}

/**
 * Maps [ColorInputType] of Domain layer to its representation in Data layer (DTO)
 * and vice versa.
 */
private object ColorInputTypeMapper {

    private val valueToDtoStringMap = mapOf(
        ColorInputType.Hex to "hex",
        ColorInputType.Rgb to "rgb",
        ColorInputType.Hsv to "hsv",
    )

    init {
        val registeredTypes = valueToDtoStringMap.keys
        val allTypes = ColorInputType.entries.toSet()
        check(registeredTypes == allTypes) { "You forgot to register a new type" }
    }

    fun String.toColorInputType(): ColorInputType =
        valueToDtoStringMap.entries
            .first { entry -> entry.value == this }
            .key

    fun ColorInputType.toDtoString(): String =
        valueToDtoStringMap.getValue(this)
}

/**
 * Maps [UiColorScheme] of Domain layer to its representation in Data layer (DTO)
 * and vice versa.
 */
private object UiColorSchemeMapper {

    private val valueToDtoStringMap = mapOf(
        UiColorScheme.Light to "light",
        UiColorScheme.Dark to "dark",
        UiColorScheme.Jungle to "jungle",
        UiColorScheme.Midnight to "midnight",
    )

    init {
        val registeredTypes = valueToDtoStringMap.keys
        val allTypes = UiColorScheme.entries.toSet()
        check(registeredTypes == allTypes) { "You forgot to register a new type" }
    }

    fun String.toUiColorScheme(): UiColorScheme =
        valueToDtoStringMap.entries
            .first { entry -> entry.value == this }
            .key

    fun UiColorScheme.toDtoString(): String =
        valueToDtoStringMap.getValue(this)
}