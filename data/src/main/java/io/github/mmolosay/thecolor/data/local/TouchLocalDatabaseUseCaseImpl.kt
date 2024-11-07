package io.github.mmolosay.thecolor.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.github.mmolosay.thecolor.domain.usecase.TouchLocalDatabaseUseCase
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named
import kotlin.time.measureTime

/**
 * This implementation should access all local databases that exist in the app.
 */
class TouchLocalDatabaseUseCaseImpl @Inject constructor(
    @Named("UserPreferences") private val userPreferences: DataStore<Preferences>,
) : TouchLocalDatabaseUseCase {

    override suspend fun invoke() {
        run {
            val elapsed = measureTime {
                touchUserPreferences()
            }
            Timber
                .tag(this::class.simpleName.orEmpty())
                .i("Touching \'User Preferences\' DB took $elapsed.")
        }
    }

    private suspend fun touchUserPreferences() {
        userPreferences.data.first() // takes around 40-100 ms; subsequent accesses take ~5ms
    }
}