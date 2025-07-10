package io.github.mmolosay.thecolor.utils

import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext

suspend fun <E> ReceiveChannel<E>.receiveAllUntil(element: E) {
    while (coroutineContext.isActive) {
        val received = this.receive()
        if (received == element) break
    }
}