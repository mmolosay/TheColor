package io.github.mmolosay.thecolor.utils

data class ActionWithResult<R>(
    val result: AckValue<R>? = null,
    val action: () -> Unit, // placed last to enable trailing lambda syntax
)

fun <R> NoOpActionWithResult(): ActionWithResult<R> =
    ActionWithResult(
        result = null,
        action = ::doNothing,
    )

operator fun ActionWithResult<*>.invoke() =
    this.action.invoke()