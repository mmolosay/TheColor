package io.github.mmolosay.thecolor.composition

import io.github.mmolosay.thecolor.utils.Lens
import io.github.mmolosay.thecolor.utils.mapState
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// TODO: Transition and Reduce should be defined inside the Store once 'nested type aliases' is
//  stable
typealias Transition<S, O> = (S) -> Change<S, O>
typealias Reduce<S, O, CO> = (S, CO?) -> Change<S, O>

// Sealed, so that every store is one of this file's: none holds a state of its own below the root,
// so whatever reads a store, at once or by collecting, gets the root's state as it is at that read
@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
// TODO: should Store inherit StateFlow, and not MutableStateFlow? Given that Store has write method of update()?
sealed interface Store<S, O> : StateFlow<S> {
    fun update(transition: Transition<S, O>)
    fun <CS, CO> slice(lens: Lens<S, CS>, reduce: Reduce<S, O, CO>): Store<CS, CO>
}

fun <S> Store(initialState: S): Store<S, Nothing> =
    RootStore(flow = MutableStateFlow(initialState))

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
private class RootStore<S>(
    private val flow: MutableStateFlow<S>,
) : Store<S, Nothing>, StateFlow<S> by flow {

    // Any thread. A state equal to the current one keeps the current instance and isn't emitted
    override fun update(transition: Transition<S, Nothing>) {
        while (true) {
            val current = flow.value
            val state = transition(current).state
            if (flow.compareAndSet(current, state)) return
        }
    }

    override fun <CS, CO> slice(
        lens: Lens<S, CS>,
        reduce: Reduce<S, Nothing, CO>,
    ): Store<CS, CO> =
        SliceStore(parent = this, lens = lens, reduce = reduce)
}

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
private class SliceStore<PS, PO, S, O>(
    val parent: Store<PS, PO>,
    private val lens: Lens<PS, S>,
    private val reduce: Reduce<PS, PO, O>,
) : Store<S, O>, StateFlow<S> by parent.mapState(lens::get) {

    override fun update(transition: Transition<S, O>) =
        parent.update { parentState ->
            val state = lens.get(parentState)
            val change = transition(state)
            when {
                // nothing happened: the parent's reducer isn't run, so it can't commit anything
                (change.state == state && change.outcome == null) -> Change(parentState)
                (change.state === state) -> reduce(parentState, change.outcome)
                else -> reduce(lens.set(parentState, change.state), change.outcome)
            }
        }

    override fun <CS, CO> slice(
        lens: Lens<S, CS>,
        reduce: Reduce<S, O, CO>,
    ): Store<CS, CO> =
        SliceStore(parent = this, lens = lens, reduce = reduce)
}
