package io.github.mmolosay.thecolor.data.local.utils

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit

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