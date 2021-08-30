package com.ordolabs.thecolor.util.struct

import com.ordolabs.thecolor.util.struct.Resource.Failure
import com.ordolabs.thecolor.util.struct.Resource.Loading
import com.ordolabs.thecolor.util.struct.Resource.Success

/**
 * Represents obtainable resource, that could be consumed by UI.
 * The `Resource` is either [Loading], [Success] or [Failure] instance.
 */
sealed class Resource<out V> {

    object Loading : Resource<Nothing>()
    data class Success<out V : Any>(val value: V) : Resource<V>()
    data class Failure<out V : Any>(val message: V, val error: Throwable?) : Resource<V>()

    /**
     *  Maps this [Resource] by applying one of specified callbacks to it
     *  depending on its actual instance.
     */
    inline fun fold(
        onLoading: () -> Unit = { },
        onSuccess: (value: V) -> Unit = { _ -> },
        onFailure: (message: V, error: Throwable?) -> Unit = { _, _ -> }
    ) = when (this) {
        is Loading -> onLoading()
        is Success -> onSuccess(this.value)
        is Failure -> onFailure(this.message, this.error)
    }

    inline fun <R> ifSuccess(action: (value: V) -> R): R? {
        return when (this) {
            is Success -> action(value)
            else -> null
        }
    }

    fun getOrNull(): V? {
        return when (this) {
            is Success -> this.value
            else -> null
        }
    }


    companion object
}

fun Resource.Companion.loading(): Resource<Nothing> {
    return Loading
}

fun <V : Any> Resource.Companion.success(value: V): Resource<V> {
    return Success(value)
}

fun <V : Any> Resource.Companion.failure(
    message: V,
    error: Throwable = RuntimeException()
): Resource<V> {
    return Failure(message, error)
}

fun <V : Any> Resource.Companion.failure(message: V): Resource<V> {
    return Failure(message, null)
}