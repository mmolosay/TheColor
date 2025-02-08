package io.github.mmolosay.thecolor.utils.cache

import io.github.mmolosay.thecolor.utils.retainLast

/**
 * An implementation of [MutationListener] that removes all elements from a cache (prunes it)
 * when cache element size surpasses [cacheSizeThreshold].
 * Doesn't remove a specified number of latest added elements. This number is represented by
 * [numberOfLatestElementsToKeep].
 */
class PruneOnSizeThreshold<T>(
    val cacheSizeThreshold: Int,
    val numberOfLatestElementsToKeep: Int,
) : MutationListener<T> {

    init {
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