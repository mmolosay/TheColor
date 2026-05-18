package io.github.mmolosay.thecolor.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.mmolosay.thecolor.data.local.utils.asDataStateResult
import io.github.mmolosay.thecolor.data.local.utils.getAsResult
import io.github.mmolosay.thecolor.data.local.utils.setOrRemoveValue
import io.github.mmolosay.thecolor.domain.color.ColorInputType
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.AutoProceedWithRandomizedColors
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.DynamicUiColors
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.ResumeFromLastSearchedColorOnStartup
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.SelectAllTextOnTextFieldFocus
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.SmartBackspace
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.UiColorScheme
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.UiColorSchemeSet
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferencesRepository
import io.github.mmolosay.thecolor.main.di.qualifiers.AppCoroutineScope
import io.github.mmolosay.thecolor.main.di.qualifiers.CoroutineDispatcherDiQualifiers.IoDispatcher
import io.github.mmolosay.thecolor.main.di.qualifiers.DataStoreDiQualifiers.UserPreferences
import io.github.mmolosay.thecolor.utils.DataState
import io.github.mmolosay.thecolor.utils.map
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
            .map { DataState.Ready(it) }
            .stateEagerlyInAppScope(initialValue = DataState.BeingInitialized)

    private fun Preferences.getColorInputType(): DataState.Result<ColorInputType> =
        getAsResult(DataStoreKeys.ColorInputType)
            .asDataStateResult()
            .map { dtoValue ->
                with(ColorInputTypeMapper) { dtoValue.toColorInputType() }
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
            .map { DataState.Ready(it) }
            .stateEagerlyInAppScope(initialValue = DataState.BeingInitialized)

    private fun Preferences.getAppUiColorSchemeSet(): DataState.Result<UiColorSchemeSet> {
        fun Preferences.getAppUiColorScheme(key: Preferences.Key<String>): DataState.Result<UiColorScheme> =
            getAsResult(key)
                .asDataStateResult()
                .map { dtoValue ->
                    with(UiColorSchemeMapper) { dtoValue.toUiColorScheme() }
                }

        val lightResult = getAppUiColorScheme(DataStoreKeys.AppUiColorSchemeLight)
        val darkResult = getAppUiColorScheme(DataStoreKeys.AppUiColorSchemeDark)
        if (lightResult is DataState.Result.HasValue && darkResult is DataState.Result.HasValue) {
            val value = UiColorSchemeSet(light = lightResult.value, dark = darkResult.value)
            return DataState.Result.HasValue(value)
        }
        // Result.HasValue has been checked above, so block with error() will never be executed
        return lightResult.map { error("unexpected DataState") }
    }

    override suspend fun setAppUiColorSchemeSet(value: UiColorSchemeSet?) {
        withContext(ioDispatcher) {
            dataStore.edit { preferences ->
                if (value == null) {
                    preferences.remove(DataStoreKeys.AppUiColorSchemeLight)
                    preferences.remove(DataStoreKeys.AppUiColorSchemeDark)
                    return@edit
                }

                run {
                    val lightColorSchemeDtoValue =
                        with(UiColorSchemeMapper) { value.light.toDtoString() }
                    preferences[DataStoreKeys.AppUiColorSchemeLight] = lightColorSchemeDtoValue
                }
                run {
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
            .map { DataState.Ready(it) }
            .stateEagerlyInAppScope(initialValue = DataState.BeingInitialized)

    private fun Preferences.getDynamicUiColors(): DataState.Result<DynamicUiColors> =
        getAsResult(DataStoreKeys.DynamicUiColors)
            .asDataStateResult()
            .map { dtoValue ->
                DynamicUiColors(enabled = dtoValue) // boolean stays boolean in both Data and Domain layers
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
            .map { DataState.Ready(it) }
            .stateEagerlyInAppScope(initialValue = DataState.BeingInitialized)

    private fun Preferences.getResumeFromLastSearchedColorOnStartup(): DataState.Result<ResumeFromLastSearchedColorOnStartup> =
        getAsResult(DataStoreKeys.ShouldResumeFromLastSearchedColorOnStartup)
            .asDataStateResult()
            .map { dtoValue ->
                ResumeFromLastSearchedColorOnStartup(enabled = dtoValue) // boolean stays boolean in both Data and Domain layers
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
            .map { DataState.Ready(it) }
            .stateEagerlyInAppScope(initialValue = DataState.BeingInitialized)

    private fun Preferences.getSmartBackspace(): DataState.Result<SmartBackspace> =
        getAsResult(DataStoreKeys.SmartBackspace)
            .asDataStateResult()
            .map { dtoValue ->
                SmartBackspace(enabled = dtoValue) // boolean stays boolean in both Data and Domain layers
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
            .map { DataState.Ready(it) }
            .stateEagerlyInAppScope(initialValue = DataState.BeingInitialized)

    private fun Preferences.getSelectAllTextOnTextFieldFocus(): DataState.Result<SelectAllTextOnTextFieldFocus> =
        getAsResult(DataStoreKeys.SelectAllTextOnTextFieldFocus)
            .asDataStateResult()
            .map { dtoValue ->
                SelectAllTextOnTextFieldFocus(enabled = dtoValue) // boolean stays boolean in both Data and Domain layers
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
            .map { DataState.Ready(it) }
            .stateEagerlyInAppScope(initialValue = DataState.BeingInitialized)

    private fun Preferences.getAutoProceedWithRandomizedColors(): DataState.Result<AutoProceedWithRandomizedColors> =
        getAsResult(DataStoreKeys.AutoProceedWithRandomizedColors)
            .asDataStateResult()
            .map { dtoValue ->
                AutoProceedWithRandomizedColors(enabled = dtoValue) // boolean stays boolean in both Data and Domain layers
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