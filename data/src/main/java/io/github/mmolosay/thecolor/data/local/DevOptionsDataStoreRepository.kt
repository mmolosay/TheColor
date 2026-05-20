package io.github.mmolosay.thecolor.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.mmolosay.thecolor.data.local.utils.asPrefStateResult
import io.github.mmolosay.thecolor.data.local.utils.getAsResult
import io.github.mmolosay.thecolor.data.local.utils.setOrRemoveValue
import io.github.mmolosay.thecolor.domain.dev.options.DevOptions.HttpLogging
import io.github.mmolosay.thecolor.domain.dev.options.DevOptions.PredictableRandomColors
import io.github.mmolosay.thecolor.domain.dev.options.DevOptions.StrictMode
import io.github.mmolosay.thecolor.domain.dev.options.DevOptionsRepository
import io.github.mmolosay.thecolor.domain.utils.PrefState
import io.github.mmolosay.thecolor.domain.utils.map
import io.github.mmolosay.thecolor.main.di.qualifiers.AppCoroutineScope
import io.github.mmolosay.thecolor.main.di.qualifiers.CoroutineDispatcherDiQualifiers.IoDispatcher
import io.github.mmolosay.thecolor.main.di.qualifiers.DataStoreDiQualifiers.DevOptions
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [DevOptionsRepository] powered by DataStore library.
 */
@Singleton
class DevOptionsDataStoreRepository @Inject constructor(
    @DevOptions private val dataStore: DataStore<Preferences>,
    @AppCoroutineScope private val appScope: CoroutineScope,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : DevOptionsRepository {

    override val flowOfPredictableRandomColors: StateFlow<PrefState<PredictableRandomColors>> =
        StateFlowFromDataStore(
            getValue = { it.getPredictableRandomColors() },
        )

    private fun Preferences.getPredictableRandomColors(): PrefState.Result<PredictableRandomColors> =
        getAsResult(DataStoreKeys.PredictableRandomColors)
            .asPrefStateResult()
            .map { dtoValue ->
                with(PredictableRandomColorsMapper) { dtoValue.toPredictableRandomColors() }
            }

    override suspend fun setPredictableRandomColors(value: PredictableRandomColors?) {
        withContext(ioDispatcher) {
            dataStore.setOrRemoveValue(
                key = DataStoreKeys.PredictableRandomColors,
                value = with(PredictableRandomColorsMapper) { value?.toDtoString() },
            )
        }
    }

    override val flowOfStrictMode: StateFlow<PrefState<StrictMode>> =
        StateFlowFromDataStore(
            getValue = { it.getStrictMode() },
        )

    private fun Preferences.getStrictMode(): PrefState.Result<StrictMode> =
        getAsResult(DataStoreKeys.StrictMode)
            .asPrefStateResult()
            .map { dtoValue ->
                StrictMode(enabled = dtoValue) // boolean stays boolean in both Data and Domain layers
            }

    override suspend fun setStrictMode(value: StrictMode?) {
        withContext(ioDispatcher) {
            dataStore.setOrRemoveValue(
                key = DataStoreKeys.StrictMode,
                value = value?.enabled,
            )
        }
    }

    override val flowOfHttpLogging: StateFlow<PrefState<HttpLogging>> =
        StateFlowFromDataStore(
            getValue = { it.getHttpLogging() },
        )

    private fun Preferences.getHttpLogging(): PrefState.Result<HttpLogging> =
        getAsResult(DataStoreKeys.HttpLogging)
            .asPrefStateResult()
            .map { dtoValue ->
                HttpLogging(enabled = dtoValue) // boolean stays boolean in both Data and Domain layers
            }

    override suspend fun setHttpLogging(value: HttpLogging?) {
        withContext(ioDispatcher) {
            dataStore.setOrRemoveValue(
                key = DataStoreKeys.HttpLogging,
                value = value?.enabled,
            )
        }
    }

    override suspend fun clear() {
        withContext(ioDispatcher) {
            dataStore.edit { it.clear() }
        }
    }

    private fun <T> StateFlowFromDataStore(
        getValue: (Preferences) -> PrefState.Result<T>,
    ): StateFlow<PrefState<T>> =
        dataStore.data
            .map(getValue)
            .map { PrefState.Ready(it) }
            .stateIn(
                scope = appScope,
                started = SharingStarted.Eagerly, // get values ready before first collection
                initialValue = PrefState.BeingInitialized,
            )

    private object DataStoreKeys {
        val PredictableRandomColors = stringPreferencesKey("predictable_random_colors")
        val StrictMode = booleanPreferencesKey("strict_mode")
        val HttpLogging = booleanPreferencesKey("http_logging")
    }
}

/**
 * Maps [PredictableRandomColors] of domain layer to its representation in data layer (DTO)
 * and vice versa.
 */
private object PredictableRandomColorsMapper {

    private val valueToDtoStringMap = mapOf(
        PredictableRandomColors.Random to "random",
        PredictableRandomColors.CyclingRgb to "cycling_rgb",
        PredictableRandomColors.CyclingLightDark to "cycling_light_dark",
    )

    init {
        val registeredTypes = valueToDtoStringMap.keys
        val allTypes = PredictableRandomColors.entries.toSet()
        check(registeredTypes == allTypes) { "You forgot to register a new type" }
    }

    fun String.toPredictableRandomColors(): PredictableRandomColors =
        valueToDtoStringMap.entries
            .first { entry -> entry.value == this }
            .key

    fun PredictableRandomColors.toDtoString(): String =
        valueToDtoStringMap.getValue(this)
}