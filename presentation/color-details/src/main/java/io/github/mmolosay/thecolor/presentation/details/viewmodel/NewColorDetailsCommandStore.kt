package io.github.mmolosay.thecolor.presentation.details.viewmodel

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import javax.inject.Inject

class ColorDetailsCommandStore(
    override val channel: Channel<ColorDetailsCommand> = defaultChannel(),
) : NewColorDetailsCommandProvider {

    @Inject
    constructor() : this(
        channel = defaultChannel()
    )

    companion object {
        fun defaultChannel() = Channel<ColorDetailsCommand>(
            capacity = Channel.BUFFERED,
            onBufferOverflow = BufferOverflow.SUSPEND,
            onUndeliveredElement = null,
        )
    }
}

interface NewColorDetailsCommandProvider {
    val channel: ReceiveChannel<ColorDetailsCommand>
}