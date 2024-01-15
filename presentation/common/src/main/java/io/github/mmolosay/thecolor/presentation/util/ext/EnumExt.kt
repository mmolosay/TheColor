package io.github.mmolosay.thecolor.presentation.util.ext

inline fun <reified E : Enum<E>> getFromEnum(ordinal: Int): E {
    return enumValues<E>()[ordinal]
}

inline fun <reified E : Enum<E>> getFromEnumOrNull(ordinal: Int?): E? {
    ordinal ?: return null
    return enumValues<E>().getOrNull(ordinal)
}

inline fun <reified E : Enum<E>> getFromEnumCoerced(ordinal: Int): E {
    return enumValues<E>()[ordinal % getEnumSize<E>()]
}

inline fun <reified E : Enum<E>> getNextFor(entry: E): E {
    return getFromEnumCoerced<E>(entry.ordinal + 1)
}

inline fun <reified E : Enum<E>> getNextOrNullFor(entry: E): E? {
    return getFromEnumOrNull<E>(entry.ordinal + 1)
}

inline fun <reified E : Enum<E>> getEnumSize(): Int {
    return enumValues<E>().size
}