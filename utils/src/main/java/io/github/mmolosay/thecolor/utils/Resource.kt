package io.github.mmolosay.thecolor.utils

import io.github.mmolosay.thecolor.utils.Resource.Empty
import io.github.mmolosay.thecolor.utils.Resource.Failure
import io.github.mmolosay.thecolor.utils.Resource.Loading
import io.github.mmolosay.thecolor.utils.Resource.Success

/**
 * Represents obtainable resource, that could be consumed by UI.
 * The `Resource` is either [Empty], [Loading], [Success] or [Failure] instance.
 */
sealed class Resource<out V> {

    /**
     * Represents empty, unset value. It may was cleared, or was never set.
     */
    data object Empty : Resource<Nothing>()

    /**
     * Represents loading state. If it set, then either [Success] or [Failure]
     * are going to be set in observable future.
     */
    data object Loading : Resource<Nothing>()

    /**
     * Represents success state with obtained [value].
     *
     * @param value current value.
     */
    class Success<out V>(val value: V) : Resource<V>()

    /**
     * Represents failure, occurred while obtaining resource. [payload] can be string message,
     * int code or anything else. Any [Throwable] set can be obtained from [error].
     *
     * @param payload some useful data.
     * @param error [Throwable] caught.
     */
    class Failure<out V, out P>(
        val payload: P?,
        val error: Throwable
    ) : Resource<V>() {

        class MessageException(msg: String) : Throwable(msg)
    }

    val isEmpty: Boolean
        get() = (this is Empty)

    val isLoading: Boolean
        get() = (this is Loading)

    val isSuccess: Boolean
        get() = (this is Success)

    val isFailure: Boolean
        get() = (this is Failure<*, *>)

    /**
     *  Maps this [Resource] by applying one of specified callbacks to it
     *  depending on its actual instance.
     */
    inline fun fold(
        onEmpty: () -> Unit = { },
        onLoading: () -> Unit = { },
        onSuccess: (value: V) -> Unit = { _ -> },
        onFailure: (payload: Any?, error: Throwable) -> Unit = { _, _ -> }
    ) =
        when (this) {
            is Empty -> onEmpty()
            is Loading -> onLoading()
            is Success -> onSuccess(this.value)
            is Failure<*, *> -> onFailure(this.payload, this.error)
        }

    inline fun <R> ifEmpty(action: () -> R): R? {
        return when (this) {
            is Empty -> action()
            else -> null
        }
    }

    inline fun <R> ifLoading(action: () -> R): R? {
        return when (this) {
            is Loading -> action()
            else -> null
        }
    }

    inline fun <R> ifSuccess(action: (value: V) -> R): R? {
        return when (this) {
            is Success -> action(value)
            else -> null
        }
    }

    companion object
}

// region Resource.Companion Extensions

fun <V> Resource.Companion.empty(): Resource<V> =
    Empty

fun Resource.Companion.loading(): Resource<Nothing> =
    Loading

fun <V : Any> Resource.Companion.success(value: V): Resource<V> =
    Success(value)

// endregion

// region Resource Extensions

fun <V> Resource<V>.empty(): Resource<V> =
    Empty

fun <V> Resource<V>.loading(): Resource<V> =
    Loading

fun <V, P : Any> Resource<V>.failure(payload: P, error: Throwable): Resource<V> =
    Failure(payload, error)

fun <V> Resource<V>.failure(error: Throwable): Resource<V> =
    Failure(
        payload = null,
        error = error
    )

fun <V> Resource<V>.failure(message: String): Resource<V> =
    Failure(
        payload = null,
        error = Failure.MessageException(message)
    )

// endregion

fun <V : Any> Resource<V>.getOrNull(): V? =
    when (this) {
        is Success -> this.value
        else -> null
    }