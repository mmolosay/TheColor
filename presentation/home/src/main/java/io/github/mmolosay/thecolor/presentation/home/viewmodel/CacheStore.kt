package io.github.mmolosay.thecolor.presentation.home.viewmodel

class CacheStore {

    private val mapOfTagsToCaches = mutableMapOf<Any, MutableList<*>>()

    fun <T> getOrNew(tag: Any): MutableList<T> {
        val existingCache = mapOfTagsToCaches[tag]
        @Suppress("UNCHECKED_CAST")
        if (existingCache != null) return existingCache as MutableList<T>
        val newCache = mutableListOf<T>()
        mapOfTagsToCaches[tag] = newCache
        return newCache
    }
}