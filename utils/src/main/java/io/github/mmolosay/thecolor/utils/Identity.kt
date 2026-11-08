package io.github.mmolosay.thecolor.utils

/**
 * What tells an object apart from every other one that holds the same values.
 *
 * An instance is equal only to itself. A data class that holds a new one in each object it creates
 * is therefore equal only to that object and its copies, whatever else it holds. Suits what stands
 * for one occurrence, such as a request for work, so that a request made again is never taken for
 * the one before it.
 */
class Identity
