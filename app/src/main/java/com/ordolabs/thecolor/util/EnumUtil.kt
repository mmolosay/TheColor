package com.ordolabs.thecolor.util

inline fun <reified E : Enum<E>> getFromEnum(ordinal: Int): E {
    return enumValues<E>()[ordinal]
}

inline fun <reified E : Enum<E>> getFromEnumOrNull(ordinal: Int): E? {
    return enumValues<E>().getOrNull(ordinal)
}