package io.github.mmolosay.thecolor.utils.cache

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