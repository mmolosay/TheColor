package io.github.mmolosay.thecolor.composition

import io.github.mmolosay.thecolor.utils.Lens
import io.github.mmolosay.thecolor.utils.mapState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.EmptyCoroutineContext

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

// 'main' is the dispatcher of the thread that Views run on
fun <S> Store(
    initialState: S,
    main: CoroutineDispatcher = Dispatchers.Main.immediate,
): Store<S, Nothing> =
    RootStore(flow = MutableStateFlow(initialState), main = main)

// Calls 'listener' on the main thread after every commit that changes the root's state: inside the
// commit if it's made there, otherwise in a later main-thread task. Every listener is the root's,
// whichever store it's added through, so it hears commits made anywhere on the screen
internal fun Store<*, *>.listen(listener: () -> Unit): AutoCloseable =
    when (this) {
        is RootStore<*> -> addListener(listener)
        is SliceStore<*, *, *, *> -> parent.listen(listener)
    }

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
private class RootStore<S>(
    private val flow: MutableStateFlow<S>,
    private val main: CoroutineDispatcher,
) : Store<S, Nothing>, StateFlow<S> by flow {

    private val listeners = CopyOnWriteArrayList<() -> Unit>()
    private val isNotificationPosted = AtomicBoolean(false)

    // Any thread. A state equal to the current one keeps the current instance, isn't emitted and
    // calls no listener
    override fun update(transition: Transition<S, Nothing>) {
        while (true) {
            val current = flow.value
            val state = transition(current).state
            if (flow.compareAndSet(current, state)) {
                if (state != current) notifyListeners()
                return
            }
        }
    }

    override fun <CS, CO> slice(
        lens: Lens<S, CS>,
        reduce: Reduce<S, Nothing, CO>,
    ): Store<CS, CO> =
        SliceStore(parent = this, lens = lens, reduce = reduce)

    fun addListener(listener: () -> Unit): AutoCloseable {
        listeners += listener
        return AutoCloseable { listeners -= listener }
    }

    // On the main thread, the listeners run inside the commit, so a View has its own action's change
    // before the action returns. A commit made elsewhere is passed on in one later main-thread task,
    // which reads the newest state when it runs, so the commits made before it runs share it
    private fun notifyListeners() {
        if (!main.isDispatchNeeded(EmptyCoroutineContext)) {
            listeners.forEach { listener -> listener() }
            return
        }
        if (isNotificationPosted.compareAndSet(false, true)) {
            main.dispatch(EmptyCoroutineContext) {
                // cleared first, so that a commit made while the listeners run posts another task
                isNotificationPosted.set(false)
                listeners.forEach { listener -> listener() }
            }
        }
    }
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
