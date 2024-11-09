package io.github.mmolosay.thecolor.presentation.input.impl.model

/**
 * Encapsulates a command to update some data of type [T] with new [payload].
 *
 * @param payload new data to be set.
 * @param causedByUser whether this [Update] is caused by changes in View made by user or not.
 */
data class Update<T>(
    val payload: T,
    val causedByUser: Boolean,
)

/** Syntactic sugar for creating [Update]. */
internal infix fun <T> T.causedByUser(causedByUser: Boolean) =
    Update(payload = this, causedByUser = causedByUser)

/**
 * Maps data of receiver to different type.
 */
internal inline fun <T, R> Update<T>.map(
    transform: (T) -> R,
) =
    Update(
        payload = transform(this.payload),
        causedByUser = causedByUser,
    )