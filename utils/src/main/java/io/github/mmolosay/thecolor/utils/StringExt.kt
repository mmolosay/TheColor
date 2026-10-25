package io.github.mmolosay.thecolor.utils

fun String.doubleEveryChar(): String {
    val result = StringBuilder()
    for (char in this) {
        repeat(2) { result.append(char) }
    }
    return result.toString()
}