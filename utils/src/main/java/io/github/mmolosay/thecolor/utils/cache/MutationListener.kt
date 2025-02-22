package io.github.mmolosay.thecolor.utils.cache

/**
 * Reacts to changes in specified [Cache].
 */
fun interface MutationListener<T> {
    fun onChanged(cache: Cache<T>)
}