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
        val previousEventDirection = previousEvent?.direction()
        val currentEventDirection = event.direction()
        val directionChange =
            lifecycleDirectionChange(previousEventDirection, currentEventDirection)
        extendedObserver.onStateChanged(
            source = source,
            event = event,
            directionChange = directionChange,
        )
        previousEvent = event
    }

    private fun lifecycleDirectionChange(
        previous: LifecycleEventDirection?,
        current: LifecycleEventDirection,
    ): LifecycleDirectionChangeEvent? =
        when (previous) {
            LifecycleEventDirection.Foreground -> when (current) {
                LifecycleEventDirection.Foreground -> null
                LifecycleEventDirection.Background -> LifecycleDirectionChangeEvent.LeavingForeground
            }
            LifecycleEventDirection.Background -> when (current) {
                LifecycleEventDirection.Foreground -> LifecycleDirectionChangeEvent.EnteringForeground
                LifecycleEventDirection.Background -> null
            }
            null -> when (current) {
                LifecycleEventDirection.Foreground -> LifecycleDirectionChangeEvent.EnteringForeground
                LifecycleEventDirection.Background -> LifecycleDirectionChangeEvent.LeavingForeground
            }
        }

    private fun Lifecycle.Event.direction(): LifecycleEventDirection =
        when (this) {
            Lifecycle.Event.ON_CREATE,
            Lifecycle.Event.ON_START,
            Lifecycle.Event.ON_RESUME,
                -> LifecycleEventDirection.Foreground
            Lifecycle.Event.ON_PAUSE,
            Lifecycle.Event.ON_STOP,
            Lifecycle.Event.ON_DESTROY,
                -> LifecycleEventDirection.Background
            Lifecycle.Event.ON_ANY ->
                error("unreachable")
        }

    /** Describes a direction towards which a particular [Lifecycle.Event] is moving. */
    private enum class LifecycleEventDirection {
        Foreground,
        Background;
    }
}