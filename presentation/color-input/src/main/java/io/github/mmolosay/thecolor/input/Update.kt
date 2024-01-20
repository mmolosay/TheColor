package io.github.mmolosay.thecolor.input

/**
 * Encapsulates a command to update some data of type [T] with new [data].
 *
 * @param data new data to be set.
 * @param causedByUser whether this [Update] is cause by changes in View made by user.
 */
data class Update<T>(
    val data: T,
    val causedByUser: Boolean,
)

/** Syntactic sugar for creating [Update]. */
infix fun <UiData> UiData.causedByUser(causedByUser: Boolean) =
    Update(data = this, causedByUser = causedByUser)

/**
 * Maps data of receiver to different type.
 */
inline fun <T, R> Update<T>.map(
    transform: (T) -> R,
) =
    Update(
        data = transform(this.data),
        causedByUser = causedByUser,
    )