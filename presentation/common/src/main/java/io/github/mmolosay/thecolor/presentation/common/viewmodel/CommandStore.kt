package io.github.mmolosay.thecolor.presentation.common.viewmodel

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel

interface CommandStore<C> {
    val channel: Channel<C>
}

fun <C> CommandStore(
    channel: Channel<C> = CommandStoreDefaults.channel(),
): CommandStore<C> =
    CommandStoreImpl<C>(
        channel = channel,
    )

private class CommandStoreImpl<C>(
    override val channel: Channel<C>,
) : CommandStore<C>

object CommandStoreDefaults {

    fun <C> channel(): Channel<C> =
        Channel<C>(
            capacity = Channel.BUFFERED,
            onBufferOverflow = BufferOverflow.SUSPEND,
            onUndeliveredElement = null,
        )
}