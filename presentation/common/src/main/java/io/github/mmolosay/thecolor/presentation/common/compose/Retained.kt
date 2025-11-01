package io.github.mmolosay.thecolor.presentation.common.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay

/**
 * Allows to retain and [remember] changing value according to [retentionSpec].
 *
 * Every time this function is called with a new [actualValue], a [retentionSpec] is executed.
 * In there caller decides what to do with a new [actualValue]: set it to memoized value immediately,
 * or set it after some [delay], or ignore it whatsoever.
 *
 * Employs [LaunchedEffect] to execute [retentionSpec] when a new [actualValue] arrives.
 * Thus, if [retentionSpec] is still being executed for a previous value, then spec execution is cancelled.
 *
 * Doesn't call [retentionSpec] on first composition, because memoized value is already equal to
 * initial [actualValue].
 *
 * Doesn't call [retentionSpec] if updated [actualValue] is already equal to memoized value.
 */
@Suppress("NOTHING_TO_INLINE")
@Composable
inline fun <T> retained(
    actualValue: T,
    retentionSpec: RetentionSpec<T>,
): MutableState<T> {
    val stateOfMemoizedValue = remember { mutableStateOf(actualValue) }
    LaunchedEffect(actualValue) {
        if (stateOfMemoizedValue.value == actualValue) return@LaunchedEffect
        with(retentionSpec) {
            stateOfMemoizedValue.invoke(actualValue)
        }
    }
    return stateOfMemoizedValue
}

/**
 * Retains only not-null values.
 *
 * Initial [actualValue] may be `null`. It will be used as initial retained value.
 * Every time this function is called with a new [actualValue], a memoized value will only be
 * updated if an actual new value is not `null`.
 * This way, the function returns either a last not-null [actualValue], or `null` if there were
 * no such yet.
 *
 * @see retained
 */
@Suppress("NOTHING_TO_INLINE")
@Composable
inline fun <T> retainedNotNull(
    actualValue: T?,
): MutableState<T?> =
    retained(actualValue) { actual ->
        if (actual != null) {
            value = actual
        }
    }

fun interface RetentionSpec<T> {
    suspend operator fun MutableState<T>.invoke(actualValue: T)
}