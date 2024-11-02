package io.github.mmolosay.thecolor.presentation.impl

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
fun buildLifecycleObserver(
    builderAction: LifecycleObserverBuilderScope.() -> Unit,
): LifecycleObserver {
    contract {
        callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE)
    }
    val scope = LifecycleObserverBuilderScopeImpl()
    scope.builderAction()
    return scope.build()
}

interface LifecycleObserverBuilderScope {
    fun onStart(action: () -> Unit)
    fun onPause(action: () -> Unit)
}

private class LifecycleObserverBuilderScopeImpl : LifecycleObserverBuilderScope {

    private val mapOfEventsToActions = mutableMapOf<Lifecycle.Event, () -> Unit>()

    override fun onStart(action: () -> Unit) =
        Lifecycle.Event.ON_START.associateWith(action)

    override fun onPause(action: () -> Unit) =
        Lifecycle.Event.ON_PAUSE.associateWith(action)

    private fun Lifecycle.Event.associateWith(action: () -> Unit) {
        mapOfEventsToActions[this] = action
    }

    fun build(): LifecycleObserver =
        LifecycleEventObserver { _, event ->
            val action = mapOfEventsToActions[event]
            action?.invoke()
        }
}