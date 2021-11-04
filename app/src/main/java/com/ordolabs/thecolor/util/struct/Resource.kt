package com.ordolabs.thecolor.util.struct

import com.ordolabs.thecolor.util.struct.Resource.Empty
import com.ordolabs.thecolor.util.struct.Resource.Failure
import com.ordolabs.thecolor.util.struct.Resource.Loading
import com.ordolabs.thecolor.util.struct.Resource.Success

/**
 * Represents obtainable resource, that could be consumed by UI.
 * The `Resource` is either [Empty], [Loading], [Success] or [Failure] instance.
 */
sealed class Resource<out V>(open val value: V?) {

    /**
     * Represents empty, unset value. It may was cleared, or was never set.
     */
    class Empty<out V>(value: V?) : Resource<V>(value)

    /**
     * Represents loading state. If it set, then either [Success] or [Failure]
     * are going to be set in observable future.
     */
    class Loading<out V>(value: V?) : Resource<V>(value)

    /**
     * Represents success state with obtained resource [value].
     */
    class Success<out V>(override val value: V) : Resource<V>(value)

    /**
     * Represents failure, occured while obtaining resource. [payload] can be string message,
     * int code or anything else. If there was any [Throwable] set, it can be obtainded from [error].
     */
    class Failure<out V, out P : Any>(
        value: V?,
        val payload: P,
        val error: Throwable?
    ) : Resource<V>(value)

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
        onEmpty: (previous: V?) -> Unit = { },
        onLoading: (previous: V?) -> Unit = { },
        onSuccess: (value: V) -> Unit = { _ -> },
        onFailure: (previous: V?, payload: Any, error: Throwable?) -> Unit = { _, _, _ -> }
    ) =
        when (this) {
            is Empty -> onEmpty(this.value)
            is Loading -> onLoading(this.value)
            is Success -> onSuccess(this.value)
            is Failure<*, *> -> onFailure(this.value, this.payload, this.error)
        }

    inline fun <R> ifEmpty(action: (value: V?) -> R): R? {
        return when (this) {
            is Empty -> action(value)
            else -> null
        }
    }

    inline fun <R> ifLoading(action: (value: V?) -> R): R? {
        return when (this) {
            is Loading -> action(value)
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

fun <V> Resource.Companion.empty(): Resource<V> {
    return Empty(value = null)
}

fun Resource.Companion.loading(): Resource<Nothing> {
    return Loading(value = null)
}

fun <V : Any> Resource.Companion.success(value: V): Resource<V> {
    return Success(value)
}

// endregion

// region Resource Extensions

fun <V> Resource<V>.empty(): Resource<V> {
    return Empty(value = this.value)
}

fun <V> Resource<V>.loading(): Resource<V> {
    return Loading(value = this.value)
}

fun <V, P : Any> Resource<V>.failure(payload: P, error: Throwable): Resource<V> {
    return Failure(this.value, payload, error)
}

fun <V, P : Any> Resource<V>.failure(payload: P): Resource<V> {
    return Failure(this.value, payload, error = null)
}

// endregion

fun <V : Any> Resource<V>.getOrNull(): V? {
    return when (this) {
        is Success -> this.value
        else -> null
    }
}