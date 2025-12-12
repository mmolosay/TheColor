package io.github.mmolosay.thecolor.presentation.common

import io.github.mmolosay.thecolor.presentation.common.ImmediateRelayDefaults.ThrowErrorOnUndeliveredElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.withContext

/*
 * Related articles:
 * 1. https://github.com/Kotlin/kotlinx.coroutines/issues/2886
 * 2. https://proandroiddev.com/android-one-off-events-approaches-evolution-anti-patterns-add887cd0250
 * 3. https://itnext.io/exercises-in-futility-one-time-events-in-android-ddbdd7b5bd1c
 */
/**
 * A one-time delivery mechanism for sending values to active collectors.
 * Designed to send one-time events from ViewModel to View, such as navigation events.
 *
 * Heavily relies on [Dispatchers.Main.immediate][kotlinx.coroutines.MainCoroutineDispatcher.immediate].
 * The method [send] will switch to it for the caller.
 * The View should use the [flowForView] to collect sent values.
 *
 * Caution: due to executing on `Dispatchers.Main.immediate`, rapidly sending many values will
 * noticeably load main thread, thus causing UI lags.
 *
 * Sent values are buffered and delivered in order to the first collector of the [flowForView].
 * If no collector is active when a value is sent, then the value will be retained and delivered when collection begins.
 * If the value is never collected and the internal [channel] is cancelled or closed while still
 * containing undelivered values, [onUndeliveredElement] will be invoked.
 */
class ImmediateRelay<T>(
    onUndeliveredElement: (T) -> Unit = ThrowErrorOnUndeliveredElement(),
) {

    private val channel = Channel<T>(
        capacity = Channel.UNLIMITED, onUndeliveredElement = onUndeliveredElement,
    )

    /*
     * Marked as 'private', because the single "allowed" collector is a 'flowForView'.
     * Multiple collectors will "fight" for each emitted value, which is not desirable. See documentation for the 'receiveAsFlow()'.
     */
    private val flow: Flow<T> = channel.receiveAsFlow()
    val flowForView: Flow<T> = FlowForView(wrapped = flow)

    suspend fun send(event: T) {
        withContext(Dispatchers.Main.immediate) {
            channel.send(event)
        }
    }

    /**
     * An implementation of the [Flow] which delegates all calls to the [wrapped] flow,
     * but performs [collect]ion on the [Dispatchers.Main.immediate][kotlinx.coroutines.MainCoroutineDispatcher.immediate].
     */
    // Flow is declared as not stable for inheritance, but I think it's OK in this case
    private class FlowForView<T>(private val wrapped: Flow<T>) : Flow<T> {

        override suspend fun collect(collector: FlowCollector<T>) =
            withContext(Dispatchers.Main.immediate) {
                wrapped.collect(collector)
            }
    }
}

object ImmediateRelayDefaults {
    fun <T> ThrowErrorOnUndeliveredElement(): (T) -> Unit = { element ->
        throw Error("Undelivered element in ${ImmediateRelay::class.simpleName}: $element")
    }
}