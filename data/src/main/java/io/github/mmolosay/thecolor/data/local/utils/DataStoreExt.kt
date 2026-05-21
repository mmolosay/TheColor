package io.github.mmolosay.thecolor.data.local.utils

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import io.github.mmolosay.thecolor.domain.utils.PrefState

/**
 * Sets the specified [key]-[value] pair to the receiver [DataStore].
 *
 * If the [value] is not `null`, then the value is set (or updated).
 * If the [value] is `null`, then the existing value by the [key] is removed from the [DataStore].
 */
internal suspend fun <T : Any> DataStore<Preferences>.setOrRemoveValue(
    key: Preferences.Key<T>,
    value: T?,
) {
    this.edit { preferences ->
        if (value != null) {
            preferences[key] = value
        } else {
            preferences.remove(key)
        }
    }
}

internal fun <T> Preferences.getAsResult(
    key: Preferences.Key<T>,
): PreferenceResult<T> {
    if (key !in this) return PreferenceResult.NoValue
    @Suppress("UNCHECKED_CAST")
    val value = try {
        this.get(key) as T
    } catch (_: ClassCastException) {
        return PreferenceResult.InvalidValue
    }
    return PreferenceResult.HasValue(value)
}

// individual class to make 'getAsResult()' independent from 'PrefState'
internal sealed interface PreferenceResult<out T> {
    data object NoValue : PreferenceResult<Nothing>
    data object InvalidValue : PreferenceResult<Nothing>
    data class HasValue<T>(val value: T) : PreferenceResult<T>
}

internal fun <T> PreferenceResult<T>.asPrefStateResult(): PrefState.Result<T> =
    when (this) {
        is PreferenceResult.NoValue -> PrefState.Result.NoValue
        is PreferenceResult.InvalidValue -> PrefState.Result.InvalidValue
        is PreferenceResult.HasValue -> PrefState.Result.HasValue(this.value)
    }