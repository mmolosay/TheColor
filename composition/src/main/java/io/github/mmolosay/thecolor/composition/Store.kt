package io.github.mmolosay.thecolor.composition

import io.github.mmolosay.thecolor.utils.Lens
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

// TODO: Write and Reduce should be defined inside the Store once 'nested type aliases' is stable
typealias Write<S, O> = (S) -> Change<S, O>
typealias Reduce<S, O, CO> = (S, CO?) -> Change<S, O>

interface Store<S, O> {
    val moment: Moment<S>
    val moments: Flow<Moment<S>>

    fun update(write: Write<S, O>)
    fun <CS, CO> slice(lens: Lens<S, CS>, reduce: Reduce<S, O, CO>): Store<CS, CO>
}

fun <S> Store(initialState: S): Store<S, Nothing> =
    RootStore(initialState)

private class RootStore<S>(initialState: S) : Store<S, Nothing> {

    private val flow = run {
        val value = Moment(initialState, version = 0L)
        MutableStateFlow(value)
    }

    override val moments: StateFlow<Moment<S>> = flow.asStateFlow()

    override val moment: Moment<S>
        get() = moments.value

    override fun update(write: Write<S, Nothing>) =
        flow.update { current ->
            val change = write(current.state)
            if (change.state == current.state) {
                return@update current // nothing changed, no need in new Moment with new version
            }
            Moment(change.state, version = current.version + 1)
        }

    override fun <CS, CO> slice(lens: Lens<S, CS>, reduce: Reduce<S, Nothing, CO>): Store<CS, CO> =
        SliceStore(parent = this, lens = lens, reduce = reduce)
}

private class SliceStore<PS, PO, S, O>(
    private val parent: Store<PS, PO>,
    private val lens: Lens<PS, S>,
    private val reduce: Reduce<PS, PO, O>,
) : Store<S, O> {

    override val moment: Moment<S>
        get() = parent.moment.focus(lens)

    override val moments: Flow<Moment<S>> =
        parent.moments
            .map { it.focus(lens) }
            .distinctUntilChangedBy { it.state }

    override fun update(write: Write<S, O>) =
        parent.update { parentState ->
            val state = lens.get(parentState)
            val change = write(state)
            val newParentState = when {
                (change.state === state) -> parentState
                else -> lens.set(parentState, change.state)
            }
            reduce(newParentState, change.outcome)
        }

    override fun <CS, CO> slice(lens: Lens<S, CS>, reduce: Reduce<S, O, CO>): Store<CS, CO> =
        SliceStore(parent = this, lens = lens, reduce = reduce)
}