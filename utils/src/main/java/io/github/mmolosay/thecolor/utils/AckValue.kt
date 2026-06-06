package io.github.mmolosay.thecolor.utils

data class AckValue<T>(
    val value: T,
    val ack: () -> Unit,
)