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