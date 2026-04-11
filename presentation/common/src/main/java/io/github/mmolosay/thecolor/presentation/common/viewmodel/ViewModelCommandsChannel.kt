package io.github.mmolosay.thecolor.presentation.common.viewmodel

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel

fun <C> ViewModelCommandsChannel(): Channel<C> =
    Channel(
        capacity = Channel.BUFFERED,
        onBufferOverflow = BufferOverflow.SUSPEND,
        onUndeliveredElement = null,
    )