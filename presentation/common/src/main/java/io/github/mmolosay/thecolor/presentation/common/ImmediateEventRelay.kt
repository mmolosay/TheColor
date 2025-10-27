package io.github.mmolosay.thecolor.presentation.common

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.withContext

/*
 * Related articles:
 * 1. https://github.com/Kotlin/kotlinx.coroutines/issues/2886
 * 2. https://proandroiddev.com/android-one-off-events-approaches-evolution-anti-patterns-add887cd0250
 * 3. https://itnext.io/exercises-in-futility-one-time-events-in-android-ddbdd7b5bd1c
 */
/**
 * A one-time event delivery mechanism for sending events to active collectors.
 * Designed to be used to send one-time events from ViewModel to View, such as navigation events.
 *
 * Heavily relies on [Dispatchers.Main.immediate][kotlinx.coroutines.MainCoroutineDispatcher.immediate].
 * [send] will switch to it for the caller, but [eventFlow] should be explicitly collected on client
 * side (in View) in the context of `Dispatchers.Main.immediate`.
 * Failing to do so may result in events being lost.
 *
 * Caution: due to executing on `Dispatchers.Main.immediate`, rapidly sending many events will
 * noticeably load main thread, thus causing UI lags.
 *
 * Sent events are buffered and delivered in order to the first collector of [eventFlow].
 * If no collector is active when an event is sent, the event will be retained and delivered when collection begins.
 * If the event is never collected and the internal [channel] is cancelled or closed while still
 * containing undelivered events, [onUndeliveredEvent] will be invoked.
 */
class ImmediateEventRelay<T : Any>(
    onUndeliveredEvent: (T) -> Unit = ThrowErrorOnUndeliveredEvent,
) {

    private val channel = Channel<T>(
        capacity = Channel.UNLIMITED, onUndeliveredElement = onUndeliveredEvent,
    )

    // MUST be collected from Dispatchers.Main.immediate
    val eventFlow: Flow<T> = channel.receiveAsFlow()

    suspend fun send(event: T) {
        withContext(Dispatchers.Main.immediate) {
            channel.send(event)
        }
    }
}

private data object ThrowErrorOnUndeliveredEvent : (Any) -> Unit {
    override operator fun invoke(event: Any) {
        throw Error("Undelivered event: $event")
    }
}