package io.github.mmolosay.thecolor.utils

import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive

suspend fun <E> ReceiveChannel<E>.receiveAllUntil(element: E) {
    while (currentCoroutineContext().isActive) {
        val received = this.receive()
        if (received == element) break
    }
}

/**
 * Performs [trySend()][SendChannel.trySend] with assertion that the [element]
 * was indeed emitted.
 * See [SendChannel.trySend] for more details.
 *
 * For [Flow][kotlinx.coroutines.flow.Flow] variation see [MutableSharedFlow.requireEmit()][kotlinx.coroutines.flow.MutableSharedFlow.requireEmit].
 */
fun <T> SendChannel<T>.requireSend(element: T): ChannelResult<Unit> =
    this.trySend(element).also { result ->
        require(!result.isFailure) { "Failed to send element $element" }
    }