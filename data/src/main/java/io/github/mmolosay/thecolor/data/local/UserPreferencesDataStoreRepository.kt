package io.github.mmolosay.thecolor.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.github.mmolosay.thecolor.domain.model.UserPreferences
import io.github.mmolosay.thecolor.domain.repository.DefaultUserPreferences
import io.github.mmolosay.thecolor.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import javax.inject.Named
import javax.inject.Singleton

/**
 * Implementation of [UserPreferencesRepository] powered by DataStore library.
 */
// instances are created using @Provides method in ':main' module
class UserPreferencesDataStoreRepository(
    private val appContext: WeakReference<Context>,
    @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher,
) : UserPreferencesRepository {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")
    private val dataStore: DataStore<Preferences>?
        get() = appContext.get()?.dataStore

    override fun flowOfColorInputType(): Flow<UserPreferences.ColorInputType> {
        val preferencesFlow = dataStore?.data ?: return emptyFlow()
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
            dataStore?.edit { preferences ->
                if (dtoValue != null) {
                    preferences[DataStoreKeys.ColorInputType] = dtoValue
                } else {
                    preferences.remove(DataStoreKeys.ColorInputType)
                }
            }
        }
    }

    private object DataStoreKeys {
        val ColorInputType = stringPreferencesKey("color_input_type")
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