package io.github.mmolosay.thecolor.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.mmolosay.thecolor.data.local.utils.setOrRemoveValue
import io.github.mmolosay.thecolor.domain.model.DevOptions.PredictableRandomColors
import io.github.mmolosay.thecolor.domain.model.DevOptions.StrictMode
import io.github.mmolosay.thecolor.domain.repository.DevOptionsRepository
import io.github.mmolosay.thecolor.domain.repository.DevOptionsRepository.DataState
import io.github.mmolosay.thecolor.domain.repository.IllegalStoredValue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Implementation of [DevOptionsRepository] powered by DataStore library.
 */
@Singleton
class DevOptionsDataStoreRepository @Inject constructor(
    @Named("DevOptions") private val dataStore: DataStore<Preferences>,
    @Named("ApplicationScope") private val appScope: CoroutineScope,
    @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher,
) : DevOptionsRepository {

    override val flowOfPredictableRandomColors: StateFlow<DataState<PredictableRandomColors>> =
        dataStore.data
            .map { it.getPredictableRandomColors() }
            .stateEagerlyInAppScope(initialValue = DataState.BeingInitialized)

    private fun Preferences.getPredictableRandomColors(): DataState<PredictableRandomColors> {
        val key = DataStoreKeys.PredictableRandomColors
        if (key !in this) return DataState.NoValueStored
        val dtoValue = this[key]
        if (dtoValue != null) {
            val value = with(PredictableRandomColorsMapper) { dtoValue.toPredictableRandomColors() }
            return DataState.HasValueStored(value)
        } else {
            throw IllegalStoredValue<PredictableRandomColors>(value = dtoValue) // 'dtoValue' is null here
        }
    }

    override suspend fun setPredictableRandomColors(value: PredictableRandomColors?) {
        withContext(ioDispatcher) {
            dataStore.setOrRemoveValue(
                key = DataStoreKeys.PredictableRandomColors,
                value = with(PredictableRandomColorsMapper) { value?.toDtoString() },
            )
        }
    }

    override val flowOfStrictMode: StateFlow<DataState<StrictMode>> =
        dataStore.data
            .map { it.getStrictMode() }
            .stateEagerlyInAppScope(initialValue = DataState.BeingInitialized)

    private fun Preferences.getStrictMode(): DataState<StrictMode> {
        val key = DataStoreKeys.StrictMode
        if (key !in this) return DataState.NoValueStored
        val dtoValue = this[key]
        if (dtoValue != null) {
            val value = StrictMode(enabled = dtoValue) // boolean stays boolean in both Data and Domain layers
            return DataState.HasValueStored(value)
        } else {
            throw IllegalStoredValue<StrictMode>(value = dtoValue) // 'dtoValue' is null here
        }
    }

    override suspend fun setStrictMode(value: StrictMode?) {
        withContext(ioDispatcher) {
            dataStore.setOrRemoveValue(
                key = DataStoreKeys.StrictMode,
                value = value?.enabled,
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
        val PredictableRandomColors = stringPreferencesKey("predictable_random_colors")
        val StrictMode = booleanPreferencesKey("strict_mode")
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