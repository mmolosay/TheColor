package com.ordolabs.thecolor.util.ext

infix fun <T> Pair<T, T>.order(firstToSecond: Boolean): Pair<T, T> {
    return if (firstToSecond) this else Pair(second, first)
}

infix fun <T> Pair<T, T>.by(selector: Boolean): T {
    return if (selector) this.first else this.second
}

infix fun Pair<Float, Float>.by(factor: Float): Float {
    return first + (second - first) * factor
}