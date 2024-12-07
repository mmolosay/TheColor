package io.github.mmolosay.thecolor.presentation.impl

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import io.github.mmolosay.thecolor.presentation.impl.ExtendedLifecycleEventObserver.LifecycleDirectionChangeEvent

/**
 * A variation of a lifecycle event observer that receives additional [LifecycleDirectionChangeEvent]
 * parameter.
 * Allows to easily execute actions when lifecycle state goes from foreground to background or
 * vice versa.
 *
 * Use [ExtendedLifecycleEventObserver.toLifecycleEventObserver] to convert it to a type of
 * lifecycle observer.
 */
fun interface ExtendedLifecycleEventObserver {

    fun onStateChanged(
        source: LifecycleOwner,
        event: Lifecycle.Event,
        directionChange: LifecycleDirectionChangeEvent?,
    )

    /**
     * An event that depicts a stream of lifecycle events changing its direction.
     */
    enum class LifecycleDirectionChangeEvent {
        EnteringForeground,
        LeavingForeground;
    }
}

fun ExtendedLifecycleEventObserver.toLifecycleEventObserver(): LifecycleEventObserver =
    ExtendedLifecycleEventObserverAdapter(this)

/**
 * Adapter between [ExtendedLifecycleEventObserver] and [LifecycleEventObserver].
 * Delegates invocations of [LifecycleEventObserver.onStateChanged] to [ExtendedLifecycleEventObserver.onStateChanged].
 */
private class ExtendedLifecycleEventObserverAdapter(
    private val extendedObserver: ExtendedLifecycleEventObserver,
) : LifecycleEventObserver {

    var previousEvent: Lifecycle.Event? = null

    override fun onStateChanged(
        source: LifecycleOwner,
        event: Lifecycle.Event,
    ) {
        val previousEventType = previousEvent?.type()
        val currentEventType = event.type()
        val directionChange = lifecycleDirectionChange(previousEventType, currentEventType)
        extendedObserver.onStateChanged(
            source = source,
            event = event,
            directionChange = directionChange,
        )
        previousEvent = event
    }

    private fun lifecycleDirectionChange(
        previous: LifecycleEventType?,
        current: LifecycleEventType,
    ): LifecycleDirectionChangeEvent? =
        when (previous) {
            LifecycleEventType.Foreground -> when (current) {
                LifecycleEventType.Foreground -> null
                LifecycleEventType.Background -> LifecycleDirectionChangeEvent.LeavingForeground
            }
            LifecycleEventType.Background -> when (current) {
                LifecycleEventType.Foreground -> LifecycleDirectionChangeEvent.EnteringForeground
                LifecycleEventType.Background -> null
            }
            null -> when (current) {
                LifecycleEventType.Foreground -> LifecycleDirectionChangeEvent.EnteringForeground
                LifecycleEventType.Background -> LifecycleDirectionChangeEvent.LeavingForeground
            }
        }

    private fun Lifecycle.Event.type(): LifecycleEventType =
        when (this) {
            Lifecycle.Event.ON_CREATE,
            Lifecycle.Event.ON_START,
            Lifecycle.Event.ON_RESUME,
                -> LifecycleEventType.Foreground
            Lifecycle.Event.ON_PAUSE,
            Lifecycle.Event.ON_STOP,
            Lifecycle.Event.ON_DESTROY,
                -> LifecycleEventType.Background
            Lifecycle.Event.ON_ANY ->
                error("unreachable")
        }

    private enum class LifecycleEventType {
        Foreground,
        Background;
    }
}