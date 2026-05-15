package io.github.mmolosay.thecolor.presentation.common.viewmodel

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Creates a [MutableSharedFlow] to be used inside a `ViewModel` to expose events of type [T]
 * that happen inside the said `ViewModel` to other components, e.g. other `ViewModel`s.
 */
fun <T> MutableViewModelEventFlow(): MutableSharedFlow<T> =
    MutableSharedFlow(
        replay = 0,
        extraBufferCapacity = 0,
        onBufferOverflow = BufferOverflow.SUSPEND,
    )