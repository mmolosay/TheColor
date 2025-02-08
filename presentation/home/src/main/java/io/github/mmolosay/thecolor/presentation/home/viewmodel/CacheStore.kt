package io.github.mmolosay.thecolor.presentation.home.viewmodel

import io.github.mmolosay.thecolor.presentation.home.viewmodel.DequeCache.MutationListener
import io.github.mmolosay.thecolor.utils.retainLast

interface Cache<T> : MutableList<T>

class DequeCache<T>(
    private val deque: ArrayDeque<T> = ArrayDeque<T>(),
    private val mutationListener: MutationListener<T>,
) : Cache<T>, MutableList<T> by deque {

    override fun add(element: T): Boolean =
        deque.add(element).also {
            mutationListener.onChanged(cache = this)
        }

    fun interface MutationListener<T> {
        fun onChanged(cache: Cache<T>)
    }
}

class SizeThresholdPruneMutationListener<T>(
    val elementsCountThreshold: Int,
    val numberOfLatestElementsToKeep: Int,
) : MutationListener<T> {

    init {
        require(numberOfLatestElementsToKeep >= 0) {
            "Requested element count $numberOfLatestElementsToKeep is less than zero."
        }
    }

    override fun onChanged(cache: Cache<T>) {
        if (cache.size > elementsCountThreshold) {
            cache.retainLast(numberOfLatestElementsToKeep)
        }
    }
}

class CacheStore {

    private val mapOfTagsToCaches = mutableMapOf<Tag, Cache<*>>()

    fun <T> getOrNew(
        tag: Tag,
        newCache: () -> Cache<T>,
    ): Cache<T> {
        val existingCache = mapOfTagsToCaches[tag]
        @Suppress("UNCHECKED_CAST")
        if (existingCache != null) return existingCache as Cache<T>
        val newCache = newCache()
        mapOfTagsToCaches[tag] = newCache
        return newCache
    }

    @JvmInline
    value class Tag(private val value: Any)
}