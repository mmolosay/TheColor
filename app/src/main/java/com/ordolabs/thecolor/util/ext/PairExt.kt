package com.ordolabs.thecolor.util.ext

infix fun <T> Pair<T, T>.by(selector: Boolean): T {
    return if (selector) this.first else this.second
}