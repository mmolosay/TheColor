package io.github.mmolosay.thecolor.utils

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex

/**
 * A "read-only" barrier to suspend at desired points.
 * One can think of it as of a reusable [CompletableDeferred].
 *
 * Commonly used in unit tests to simulate a delay that may occur during the real app's execution
 * due to non-deterministic CPU scheduling.
 */
interface SuspendGate {
    val isOpen: Boolean
    suspend fun awaitOpen()
}

val SuspendGate.isClosed: Boolean
    get() = !isOpen

/**
 * Mutable [SuspendGate]. Acts similarly to [Mutex].
 */
interface ClosableSuspendGate : SuspendGate {
    fun open()
    fun close()
}

/**
 * An implementation of [SuspendGate] that is always open.
 */
object OpenSuspendGate : SuspendGate {
    override val isOpen = true
    override suspend fun awaitOpen() {}
}

fun ClosableSuspendGate(closed: Boolean = false): ClosableSuspendGate =
    ClosableSuspendGateImpl(closed)

private class ClosableSuspendGateImpl(closed: Boolean) : ClosableSuspendGate {

    private val flowOfIsClosed = MutableStateFlow(closed)
    override val isOpen: Boolean
        get() = !flowOfIsClosed.value

    override fun open() {
        flowOfIsClosed.value = false
    }

    override fun close() {
        flowOfIsClosed.value = true
    }

    override suspend fun awaitOpen() {
        if (isOpen) return // fast route without suspension
        flowOfIsClosed.first { isClosed -> !isClosed }
    }
}