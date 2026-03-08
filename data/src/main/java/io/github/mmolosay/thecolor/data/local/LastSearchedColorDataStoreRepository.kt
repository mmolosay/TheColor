package io.github.mmolosay.thecolor.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorConverter
import io.github.mmolosay.thecolor.domain.color.LastSearchedColorRepository
import io.github.mmolosay.thecolor.main.di.qualifiers.CoroutineDispatcherDiQualifiers.IoDispatcher
import io.github.mmolosay.thecolor.main.di.qualifiers.DataStoreDiQualifiers.MiscValues
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Implementation of [LastSearchedColorRepository] powered by DataStore library.
 */
class LastSearchedColorDataStoreRepository @Inject constructor(
    @MiscValues private val dataStore: DataStore<Preferences>,
    private val colorMapper: ColorMapper,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : LastSearchedColorRepository {

    override suspend fun getLastSearchedColor(): Color? =
        withContext(ioDispatcher) {
            val preferences = dataStore.data.first()
            val dtoString = preferences[DataStoreKeys.LastSearchedColor]
            if (dtoString != null) {
                with(colorMapper) { dtoString.toColor() }
            } else {
                null
            }
        }

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