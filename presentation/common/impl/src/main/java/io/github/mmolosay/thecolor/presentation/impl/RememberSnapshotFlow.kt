package io.github.mmolosay.thecolor.presentation.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.Flow

/**
 * Creates and [remember]s a [snapshotFlow] from the given [value].
 */
@Composable
fun <T> rememberSnapshotFlow(value: T): Flow<T> {
    val updatedValue by rememberUpdatedState(value)
    val flow = remember {
        snapshotFlow { updatedValue }
    }
    return flow
}