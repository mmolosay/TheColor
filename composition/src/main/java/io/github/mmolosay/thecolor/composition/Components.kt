package io.github.mmolosay.thecolor.composition

data class Moment<out S>(
    val state: S,
    val version: Long,
)

fun <S, R> Moment<S>.map(transform: (S) -> R): Moment<R> =
    Moment(
        state = transform(this.state),
        version = this.version,
    )

data class Change<out S, out O>(
    val state: S,
    val outcome: O? = null,
)

class Token