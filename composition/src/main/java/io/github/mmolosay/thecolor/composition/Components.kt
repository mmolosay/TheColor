package io.github.mmolosay.thecolor.composition

import io.github.mmolosay.thecolor.utils.Lens

data class Moment<out S>(
    val state: S,
    val version: Long,
)

fun <S, V> Moment<S>.focus(lens: Lens<S, V>): Moment<V> =
    Moment(
        state = lens.get(this.state),
        version = this.version,
    )

data class Change<out S, out O>(
    val state: S,
    val outcome: O? = null,
)

class Token