package io.github.mmolosay.thecolor.utils.cache

import io.github.mmolosay.thecolor.utils.retainLast

/**
 * Reacts to changes in specified [Cache].
 */
fun interface MutationListener<T> {
    fun onChanged(cache: Cache<T>)
}

/**
 * An implementation of [MutationListener] that removes all elements from a cache (prunes it)
 * when cache element size surpasses [cacheSizeThreshold].
 * Doesn't remove a specified number of latest added elements. This number is represented by
 * [numberOfLatestElementsToKeep].
 */
class PruneOnSizeThreshold<T>(
    val cacheSizeThreshold: Int,
    val numberOfLatestElementsToKeep: Int = cacheSizeThreshold,
) : MutationListener<T> {

    init {
        require(cacheSizeThreshold > 0) {
            "Requested cache size $cacheSizeThreshold is less than one."
        }
        require(numberOfLatestElementsToKeep >= 0) {
            "Requested element count $numberOfLatestElementsToKeep is less than zero."
        }
    }

    override fun onChanged(cache: Cache<T>) {
        if (cache.size > cacheSizeThreshold) {
            cache.retainLast(numberOfLatestElementsToKeep)
        }
    }
}