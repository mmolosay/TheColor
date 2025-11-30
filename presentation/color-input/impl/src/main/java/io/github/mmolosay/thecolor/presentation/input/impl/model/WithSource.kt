package io.github.mmolosay.thecolor.presentation.input.impl.model

/**
 * Couples some [data] of arbitrary type [T] with the source it originates from.
 */
@ConsistentCopyVisibility
data class WithSource<T> internal constructor(
    val data: T,
    val causedByUser: Boolean,
)

/** Syntactic sugar for creating [WithSource]. */
internal infix fun <T> T.causedByUser(causedByUser: Boolean) =
    WithSource(data = this, causedByUser = causedByUser)