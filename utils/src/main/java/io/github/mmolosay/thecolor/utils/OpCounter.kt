package io.github.mmolosay.thecolor.utils

import io.github.mmolosay.thecolor.utils.OpCounter.State
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.updateAndGet
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

sealed interface OpCounter {
    val stateFlow: StateFlow<State>
    val children: List<OpCounter>

    fun update(newCount: (current: Int) -> Int)

    data class State(
        val ownCount: Int,
        val totalCount: Int,
    )
}

val OpCounter.state: State
    get() = this.stateFlow.value

fun State.totalAsLatch(): Latch {
    val hasOngoing = (this.totalCount > 0)
    return Latch(isOpen = !hasOngoing)
}

fun OpCounter(
    name: String? = null,
    vararg children: OpCounter,
    listener: OnStateChangeListener? = null,
): OpCounter =
    OpCounterImpl(
        name = name,
        children = listOf(*children),
        listener = listener,
    )

fun interface OnStateChangeListener {
    operator fun invoke(newState: State)
}

private class OpCounterImpl(
    @Suppress("unused") private val name: String?,
    override val children: List<OpCounter>,
    listener: OnStateChangeListener?,
) : OpCounter {

    private val _stateFlow = run {
        val state = State(ownCount = 0, totalCount = 0)
        MutableStateFlow(state)
    }
    override val stateFlow: StateFlow<State> = _stateFlow.asStateFlow()

    private val listeners = CopyOnWriteArrayList(listOfNotNull(listener))

    init {
        run registerChildren@{
            val listener = ParentPropagationListener()
            for (child in children) {
                (child as OpCounterImpl).listeners.add(listener)
            }
            recount()
        }
    }

    @Synchronized
    override fun update(newCount: (current: Int) -> Int) {
        val newState = _stateFlow.updateAndGet { currentState ->
            val newOwnCount = newCount(currentState.ownCount)
            require(newOwnCount >= 0) { "count cannot be less than zero" }
            val newTotalCount = countTotal(ownCount = newOwnCount)
            State(ownCount = newOwnCount, totalCount = newTotalCount)
        }
        listeners.forEach { it.invoke(newState) }
    }

    private fun recount() =
        update(newCount = { /*current*/it })

    private fun countTotal(
        childrenTotal: Int = children.sumOf { it.state.totalCount },
        ownCount: Int = state.ownCount,
    ): Int =
        childrenTotal + ownCount

    private inner class ParentPropagationListener : OnStateChangeListener {
        override fun invoke(newState: State) {
            recount()
        }
    }
}

@OptIn(ExperimentalContracts::class)
inline fun <T> OpCounter.withCounter(block: () -> T): T {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    this.update { it + 1 }
    return try {
        block()
    } finally {
        this.update { it - 1 }
    }
}