package com.ordolabs.thecolor.util.struct

import com.ordolabs.thecolor.util.struct.Resource.Empty
import com.ordolabs.thecolor.util.struct.Resource.Failure
import com.ordolabs.thecolor.util.struct.Resource.Loading
import com.ordolabs.thecolor.util.struct.Resource.Success

/**
 * Represents obtainable resource, that could be consumed by UI.
 * The `Resource` is either [Empty], [Loading], [Success] or [Failure] instance.
 */
sealed class Resource<out V> {

    /**
     * Represents empty, unset value. It may was cleared, or was never set.
     */
    object Empty : Resource<Nothing>()

    /**
     * Represents loading state. If it set, then either [Success] or [Failure]
     * are going to be set in observable future.
     */
    object Loading : Resource<Nothing>()

    /**
     * Represents success state with obtained resource [value].
     */
    data class Success<out V : Any>(val value: V) : Resource<V>()

    /**
     * Represents failure, occured while obtaining resource. [payload] can be string message,
     * int code or anything else. If there was any [Throwable] set, it can be obtainded from [error].
     */
    data class Failure<out P : Any>(val payload: P, val error: Throwable?) : Resource<Nothing>()

    val isEmpty: Boolean
        get() = (this is Empty)

    val isLoading: Boolean
        get() = (this is Loading)

    val isSuccess: Boolean
        get() = (this is Success)

    val isFailure: Boolean
        get() = (this is Failure<*>)

    /**
     *  Maps this [Resource] by applying one of specified callbacks to it
     *  depending on its actual instance.
     */
    inline fun fold(
        onEmpty: () -> Unit = { },
        onLoading: () -> Unit = { },
        onSuccess: (value: V) -> Unit = { _ -> },
        onFailure: (payload: Any, error: Throwable?) -> Unit = { _, _ -> }
    ) =
        when (this) {
            is Empty -> onEmpty()
            is Loading -> onLoading()
            is Success -> onSuccess(this.value)
            is Failure<*> -> onFailure(this.payload, this.error)
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

fun Resource.Companion.empty(): Resource<Nothing> {
    return Empty
}

fun Resource.Companion.loading(): Resource<Nothing> {
    return Loading
}

fun <V : Any> Resource.Companion.success(value: V): Resource<V> {
    return Success(value)
}

fun <P : Any> Resource.Companion.failure(payload: P, error: Throwable): Resource<Nothing> {
    return Failure(payload, error)
}

fun <P : Any> Resource.Companion.failure(payload: P): Resource<Nothing> {
    return Failure(payload, null)
}

fun <V : Any> Resource<V>.getOrNull(): V? {
    return when (this) {
        is Success -> this.value
        else -> null
    }
}