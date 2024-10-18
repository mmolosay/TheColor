package io.github.mmolosay.thecolor.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.github.mmolosay.thecolor.domain.usecase.TouchLocalDatabaseUseCase
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Named

/**
 * This implementation should access all local databases that exist in the app.
 */
class TouchLocalDatabaseUseCaseImpl @Inject constructor(
    @Named("UserPreferences") private val userPreferences: DataStore<Preferences>,
) : TouchLocalDatabaseUseCase {

    override suspend fun invoke() {
        userPreferences.data.first() // takes around 40-60 ms; subsequent accesses take ~5ms
    }
}