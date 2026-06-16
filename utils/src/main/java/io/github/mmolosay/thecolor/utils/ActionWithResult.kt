package io.github.mmolosay.thecolor.utils

data class ActionWithResult<R : Any>(
    val result: AckValue<R>? = null,
    val action: () -> Unit, // placed last to enable trailing lambda syntax
)

fun <R : Any> ActionWithResult(
    result: R?,
    resultAck: () -> Unit,
    action: () -> Unit,
): ActionWithResult<R> =
    ActionWithResult(
        result = result?.let {
            AckValue(
                value = it,
                ack = resultAck,
            )
        },
        action = action,
    )

fun <R : Any> NoOpActionWithResult(): ActionWithResult<R> =
    ActionWithResult(
        result = null,
        action = ::doNothing,
    )

operator fun ActionWithResult<*>.invoke() =
    this.action.invoke()