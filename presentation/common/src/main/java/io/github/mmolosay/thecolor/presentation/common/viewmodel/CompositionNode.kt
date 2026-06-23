package io.github.mmolosay.thecolor.presentation.common.viewmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.withContext
import java.util.concurrent.CopyOnWriteArrayList

sealed interface CompositionNode<T> {

    val id: Id
    val dataFlow: StateFlow<T>

    suspend fun update(function: (current: T) -> T)

    @JvmInline
    value class Id internal constructor(internal val int: Int)
}

val <T> CompositionNode<T>.data: T
    get() = this.dataFlow.value

internal class CompositionNodeImpl<T>(
    initialValue: T,
    override val id: CompositionNode.Id,
    private val recompute: (current: T) -> T,
    private val dispatcher: CoroutineDispatcher,
    children: List<CompositionNode<*>>,
) : CompositionNode<T> {

    private val _dataFlow = MutableStateFlow(value = initialValue)
    override val dataFlow: StateFlow<T> = _dataFlow.asStateFlow()

    private val listeners = CopyOnWriteArrayList<OnDataUpdateListener>()

    init {
        val listener = OnDataUpdateListener {
            updateAndNotify(recompute)
        }
        for (child in children) {
            (child as CompositionNodeImpl<*>).listeners += listener
        }
    }

    override suspend fun update(function: (T) -> T) {
        withContext(dispatcher) {
            ensureActive()
            updateAndNotify(function)
        }
    }

    private fun updateAndNotify(function: (T) -> T) {
        val before = _dataFlow.value
        val after = _dataFlow.updateAndGet(function)
        if (before != after) {
            for (l in listeners) l.invoke()
        }
    }

    fun interface OnDataUpdateListener {
        operator fun invoke()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class CompositionScope(
    val dispatcher: CoroutineDispatcher = Dispatchers.Default.limitedParallelism(1),
) {
    private val nodeRegistry = mutableListOf<CompositionNode<*>>()

    @Synchronized
    fun <T> node(
        initialValue: T,
        recompute: (current: T) -> T = { it },
        children: List<CompositionNode.Id> = emptyList(),
    ): CompositionNode<T> {
        val childrenNodes = children.map { childId ->
            nodeRegistry.first { it.id == childId }
        }
        return CompositionNodeImpl(
            initialValue = initialValue,
            id = CompositionNodeIdFactory.get(),
            recompute = recompute,
            dispatcher = dispatcher,
            children = childrenNodes,
        ).also {
            nodeRegistry += it
        }
    }
}

private object CompositionNodeIdFactory {
    private var nextInt = 0
    fun get() = CompositionNode.Id(nextInt++)
}