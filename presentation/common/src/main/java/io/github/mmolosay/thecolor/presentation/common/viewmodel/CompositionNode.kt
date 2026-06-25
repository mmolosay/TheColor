package io.github.mmolosay.thecolor.presentation.common.viewmodel

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.updateAndGet
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

sealed interface CompositionNode<T> {

    val id: Id
    val dataFlow: StateFlow<T>

    fun update(function: (current: T) -> T)
    fun batch(function: () -> Unit)
    fun recompute()

    @JvmInline
    value class Id internal constructor(internal val int: Int)
}

val <T> CompositionNode<T>.data: T
    get() = this.dataFlow.value

internal class CompositionNodeImpl<T>(
    initialValue: T,
    override val id: CompositionNode.Id,
    private val recompute: (current: T) -> T,
    private val lock: ReentrantLock,
    children: List<CompositionNode<*>>,
) : CompositionNode<T> {

    private val _dataFlow = MutableStateFlow(value = initialValue)
    override val dataFlow: StateFlow<T> = _dataFlow.asStateFlow()

    private val listeners = CopyOnWriteArrayList<OnDataUpdateListener>()
    private var batchDepth = 0

    init {
        val listener = OnDataUpdateListener {
            if (batchDepth == 0) {
                updateAndNotify(recompute)
            }
        }
        for (child in children) {
            (child as CompositionNodeImpl<*>).listeners += listener
        }
    }

    override fun update(function: (T) -> T) =
        lock.withLock {
            updateAndNotify(function)
        }

    override fun batch(function: () -> Unit) =
        lock.withLock {
            try {
                batchDepth++
                function()
            } finally {
                batchDepth--
                if (batchDepth == 0) {
                    updateAndNotify(recompute)
                }
            }
        }

    override fun recompute() =
        lock.withLock {
            updateAndNotify(recompute)
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
    private val lock: ReentrantLock = ReentrantLock(),
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
            lock = lock,
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