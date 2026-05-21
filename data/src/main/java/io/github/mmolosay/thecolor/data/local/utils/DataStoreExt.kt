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

internal fun <T> Preferences.getAsPrefStateResult(
    key: Preferences.Key<T>,
): PrefState.Result<T> {
    if (key !in this) return PrefState.Result.NoValue
    @Suppress("UNCHECKED_CAST")
    val value = try {
        this.get(key) as T
    } catch (_: ClassCastException) {
        return PrefState.Result.InvalidValue
    }
    return PrefState.Result.HasValue(value)
}