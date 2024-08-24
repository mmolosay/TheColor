package io.github.mmolosay.thecolor.presentation.input.impl.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Encapsulates a command to update some data of type [T] with new [payload].
 *
 * @param payload new data to be set.
 * @param causedByUser whether this [Update] is cause by changes in View made by user or not.
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

/**
 * [update]s receiver [MutableStateFlow] by changing the payload of current [Update].
 */
internal fun <T> MutableStateFlow<Update<T>?>.updatePayload(
    transform: (T) -> T,
) {
    this.update { update ->
        update?.map(transform)
    }
}