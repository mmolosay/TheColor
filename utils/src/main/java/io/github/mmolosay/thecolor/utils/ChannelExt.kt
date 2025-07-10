package io.github.mmolosay.thecolor.utils

import kotlinx.coroutines.channels.ReceiveChannel

suspend fun <E> ReceiveChannel<E>.receiveAllUntil(element: E) {
    while (true) {
        val received = this.receive()
        if (received == element) break
    }
}