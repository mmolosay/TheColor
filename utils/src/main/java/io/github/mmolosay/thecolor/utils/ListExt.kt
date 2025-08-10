package io.github.mmolosay.thecolor.utils

/**
 * Retains only last [n] elements in this collection.
 *
 * @return `true` if any element was removed from the collection, `false` if the collection was not modified.
 */
fun <T> MutableList<T>.retainLast(n: Int): Boolean {
    require(n >= 0) { "Requested element count $n is less than zero." }
    val retained = this.takeLast(n)
    return this.retainAll(retained)
}

/**
 * Removes subsequent duplicates from the list.
 *
 * Example:
 * ```
 * Given: [1, 2, 2, 3, 1, 3, 3, 3, 2]
 * Result: [1, 2, 3, 1, 3, 2]
 * ```
 */
fun <T> List<T>.removeSubsequentDuplicates(): List<T> {
    if (this.size < 2) return this
    return this.fold(initial = mutableListOf()) { acc, element ->
        if (acc.isEmpty() || acc.last() != element) acc.add(element)
        return@fold acc
    }
}