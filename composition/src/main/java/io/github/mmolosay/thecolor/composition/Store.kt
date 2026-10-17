package io.github.mmolosay.thecolor.composition

import io.github.mmolosay.thecolor.utils.Lens
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

// TODO: Transition and Reduce should be defined inside the MutableStore once 'nested type aliases'
//  is stable
typealias Transition<S, O> = (S) -> Change<S, O>
typealias Reduce<S, O, CO> = (S, CO?) -> Change<S, O>

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
interface Store<out S> : StateFlow<Moment<S>>

interface MutableStore<S, O> : Store<S> {
    fun update(transition: Transition<S, O>)
    fun <CS, CO> slice(lens: Lens<S, CS>, reduce: Reduce<S, O, CO>): MutableStore<CS, CO>
}

fun <S> MutableStore(initialState: S): MutableStore<S, Nothing> {
    val moment = Moment(initialState, version = 0L)
    return RootStore(flow = MutableStateFlow(moment))
}

// What the store holds, transformed, such as the data a View shows. Its value is read from the
// store at once, rather than copied by a coroutine after each commit, so that the result of an
// action can be read as soon as the action returns
fun <S, R> Store<S>.mapState(transform: (S) -> R): Store<R> =
    MappedStore(source = this, transform = transform)

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
private class RootStore<S>(
    private val flow: MutableStateFlow<Moment<S>>,
) : MutableStore<S, Nothing>, StateFlow<Moment<S>> by flow {

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
) : MutableStore<S, O>, Store<S> by parent.mapState(lens::get) {

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

// Emits a moment for every moment of its source, even when the result is equal, so that its
// collectors always end on its value
private class MappedStore<S, R>(
    private val source: Store<S>,
    private val transform: (S) -> R,
) : Store<R> {

    override val value: Moment<R>
        get() = source.value.map(transform)

    override val replayCache: List<Moment<R>>
        get() = listOf(value)

    override suspend fun collect(collector: FlowCollector<Moment<R>>): Nothing {
        var last: Pair<S, R>? = null // the last state given to the transform, and its result
        source.collect { moment ->
            // the same state instance gives the same result, so only a new one is transformed
            val reused = last?.takeIf { it.first === moment.state }
            val current = reused ?: Pair(moment.state, transform(moment.state))
            last = current
            collector.emit(Moment(current.second, version = moment.version))
        }
    }
}
