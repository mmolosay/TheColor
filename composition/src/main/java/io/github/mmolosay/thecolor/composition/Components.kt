package io.github.mmolosay.thecolor.composition

data class Change<out S, out O>(
    val state: S,
    val outcome: O? = null,
)

class Token
