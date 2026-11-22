package io.github.mmolosay.thecolor.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.mmolosay.thecolor.data.local.utils.getAsPrefStateResult
import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorConverter
import io.github.mmolosay.thecolor.domain.color.LastSearchedColorRepository
import io.github.mmolosay.thecolor.domain.utils.PrefState
import io.github.mmolosay.thecolor.domain.utils.map
import io.github.mmolosay.thecolor.main.di.qualifiers.AppCoroutineScope
import io.github.mmolosay.thecolor.main.di.qualifiers.CoroutineDispatcherDiQualifiers.IoDispatcher
import io.github.mmolosay.thecolor.main.di.qualifiers.DataStoreDiQualifiers.MiscValues
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
 * Implementation of [LastSearchedColorRepository] powered by DataStore library.
 */
@Singleton
class LastSearchedColorDataStoreRepository @Inject constructor(
    @MiscValues private val dataStore: DataStore<Preferences>,
    private val colorMapper: ColorMapper,
    @AppCoroutineScope private val appScope: CoroutineScope,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : LastSearchedColorRepository {

    override val flowOfLastSearchedColor: StateFlow<PrefState<Color>> =
        dataStore.data
            .map { preferences ->
                val result = preferences
                    .getAsPrefStateResult(DataStoreKeys.LastSearchedColor)
                    .map { dtoString -> with(colorMapper) { dtoString.toColor() } }
                PrefState.Ready(result)
            }
            .stateIn(
                scope = appScope,
                started = SharingStarted.Eagerly, // fetch value eagerly before first subscriber
                initialValue = PrefState.BeingInitialized,
            )

    override suspend fun setLastSearchedColor(color: Color?) {
        withContext(ioDispatcher) {
            dataStore.edit { preferences ->
                if (color == null) {
                    preferences.remove(DataStoreKeys.LastSearchedColor)
                    return@edit
                }

                val dtoString = with(colorMapper) { color.toDtoString() }
                preferences[DataStoreKeys.LastSearchedColor] = dtoString
            }
        }
    }

    private object DataStoreKeys {
        val LastSearchedColor = stringPreferencesKey("last_searched_color")
    }
}

/**
 * Maps [Color] of domain layer to its representation in data layer (DTO)
 * and vice versa.
 */
/* private but Dagger */
class ColorMapper @Inject constructor(
    private val colorConverter: ColorConverter,
) {

    fun String.toColor(): Color {
        val hexInt = this.toInt(radix = 16)
        return Color.Hex(value = hexInt)
    }

    fun Color.toDtoString(): String {
        val color = this
        val colorHex = with(colorConverter) { color.toHex() }
        val hexString = colorHex.value
            .toString(radix = 16)
            .uppercase()
            .padStart(6, '0')
        return hexString
    }
}