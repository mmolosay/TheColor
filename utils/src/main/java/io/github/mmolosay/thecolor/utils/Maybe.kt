package io.github.mmolosay.thecolor.utils

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

sealed interface Maybe<out T> {
    data object None : Maybe<Nothing>
    data class Some<T>(val value: T) : Maybe<T>
}

@OptIn(ExperimentalContracts::class)
fun <T> Maybe<T>.requireValue(): T {
    contract {
        returns() implies (this@requireValue is Maybe.Some<T>)
    }
    require(this is Maybe.Some) { "Required to have value present, but there is none" }
    return this.value
}

@OptIn(ExperimentalContracts::class)
fun <T> Maybe<T>.getOrElse(block: () -> T): T {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    return when (this) {
        is Maybe.Some -> this.value
        is Maybe.None -> block()
    }
}