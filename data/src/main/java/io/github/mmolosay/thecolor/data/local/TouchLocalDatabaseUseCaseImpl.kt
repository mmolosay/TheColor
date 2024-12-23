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
    @Named("MiscValues") private val miscValues: DataStore<Preferences>,
) : TouchLocalDatabaseUseCase {

    override suspend fun invoke() {
        kotlin.run {
            // takes around 40-100 ms; subsequent accesses take ~5ms
            val elapsed = measureTime {
                userPreferences.data.first()
            }
            Timber
                .tag(this::class.simpleName.orEmpty())
                .i("Touching \'User Preferences\' DB took $elapsed.")
        }
        // second and subsequent accesses to DataStore don't seem to be as effective as the first one
        kotlin.run {
            val elapsed = measureTime {
                miscValues.data.first()
            }
            Timber
                .tag(this::class.simpleName.orEmpty())
                .i("Touching \'Misc Values\' DB took $elapsed.")
        }
    }
}