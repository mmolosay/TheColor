package io.github.mmolosay.thecolor.utils

fun <T> Iterator<T>.nextOrNull(): T? =
    if (this.hasNext()) this.next() else null