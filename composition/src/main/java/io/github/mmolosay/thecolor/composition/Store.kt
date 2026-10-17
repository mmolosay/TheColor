package io.github.mmolosay.thecolor.composition

import io.github.mmolosay.thecolor.utils.Lens
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

// TODO: Transition and Reduce should be defined inside the MutableStore once 'nested type aliases'
//  is stable
typealias Transition<S, O> = (S) -> Change<S, O>
typealias Reduce<S, O, CO> = (S, CO?) -> Change<S, O>

interface Store<out S> {
    val moment: Moment<S>
    val moments: Flow<Moment<S>>
}

interface MutableStore<S, O> : Store<S> {
    fun update(transition: Transition<S, O>)
    fun <CS, CO> slice(lens: Lens<S, CS>, reduce: Reduce<S, O, CO>): MutableStore<CS, CO>
}

fun <S> MutableStore(initialState: S): MutableStore<S, Nothing> =
    RootStore(initialState)

private class RootStore<S>(initialState: S) : MutableStore<S, Nothing> {

    private val flow = run {
        val value = Moment(initialState, version = 0L)
        MutableStateFlow(value)
    }

    override val moments: StateFlow<Moment<S>> = flow.asStateFlow()

    override val moment: Moment<S>
        get() = moments.value

    override fun update(transition: Transition<S, Nothing>) =
        flow.update { current ->
            val change = transition(current.state)
            if (change.state == current.state) {
                return@update current // nothing changed, no need in new Moment with new version
            }
            Moment(change.state, version = current.version + 1)
        }

    override fun <CS, CO> slice(
        lens: Lens<S, CS>,
        reduce: Reduce<S, Nothing, CO>,
    ): MutableStore<CS, CO> =
        SliceStore(parent = this, lens = lens, reduce = reduce)
}

private class SliceStore<PS, PO, S, O>(
    private val parent: MutableStore<PS, PO>,
    private val lens: Lens<PS, S>,
    private val reduce: Reduce<PS, PO, O>,
) : MutableStore<S, O> {

    override val moment: Moment<S>
        get() = parent.moment.focus(lens)

    override val moments: Flow<Moment<S>> =
        parent.moments
            .map { it.focus(lens) }
            .distinctUntilChangedBy { it.state }

    override fun update(transition: Transition<S, O>) =
        parent.update { parentState ->
            val state = lens.get(parentState)
            val change = transition(state)
            when {
                // nothing happened: the parent's reducer isn't run, so it can't add a version
                (change.state == state && change.outcome == null) -> Change(parentState)
                (change.state === state) -> reduce(parentState, change.outcome)
                else -> reduce(lens.set(parentState, change.state), change.outcome)
            }
        }

    override fun <CS, CO> slice(
        lens: Lens<S, CS>,
        reduce: Reduce<S, O, CO>,
    ): MutableStore<CS, CO> =
        SliceStore(parent = this, lens = lens, reduce = reduce)
}