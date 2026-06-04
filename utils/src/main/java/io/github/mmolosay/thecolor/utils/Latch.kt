package io.github.mmolosay.thecolor.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Represents the state of a latch: either open or closed.
 */
@JvmInline
value class Latch(val isOpen: Boolean)

@Suppress("unused")
val Latch.isClosed: Boolean
    get() = !this.isOpen

val OpenLatch by lazy { Latch(isOpen = true) }
val ClosedLatch by lazy { Latch(isOpen = false) }

/**
 * This operator suppresses any emissions of the source flow while [Latch.isClosed] (data is not being updated).
 * Once the [Latch] becomes [open][Latch.isOpen] (update has finished), it emits the most recent value from the source flow.
 *
 * This is useful to prevent intermediate, unstable values from being collected while some
 * external update operation is in progress.
 *
 * You may want to apply [distinctUntilChanged] after this operator.
 * If the [latchFlow] updates without source flow emitting a new value, then the latest may be re-emitted
 * (depending on the actual value of the [Latch] update).
 *
 * @param latchFlow A flow indicating whether data is currently being updated.
 * One can think of it as of "is the source flow actively emitting non-final values" flow.
 */
fun <T> Flow<T>.through(latchFlow: Flow<Latch>): Flow<T> {
    return combineTransform(this, latchFlow) { value, latch ->
        if (latch.isOpen) emit(value)
    }
}