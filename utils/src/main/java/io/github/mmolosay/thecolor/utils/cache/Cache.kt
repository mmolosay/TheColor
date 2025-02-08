package io.github.mmolosay.thecolor.utils.cache

interface Cache<T> : MutableList<T>

class DequeCache<T>(
    private val deque: ArrayDeque<T> = ArrayDeque<T>(),
    private val mutationListener: MutationListener<T>,
) : Cache<T>, MutableList<T> by deque {

    override fun add(element: T): Boolean =
        deque.add(element).also {
            mutationListener.onChanged(cache = this)
        }
}