package io.github.mmolosay.thecolor.domain.result

import javax.inject.Inject
import kotlin.Result as KotlinResult

/**
 * Maps [KotlinResult] to domain [Result].
 */
class ResultMapper @Inject constructor(
    private val failureFactory: FailureFactory,
) {

    @Throws(UnknownThrowable::class)
    fun <T> KotlinResult<T>.toDomainResult(): Result<T> =
        this.fold(
            onSuccess = { value ->
                Result.Success(value)
            },
            onFailure = { throwable ->
                with(failureFactory) { throwable.asFailureOrNull() }
                    ?: throw UnknownThrowable(throwable)
            },
        )

    /** An exception that's thrown when specified [throwable] cannot be mapped to [Result.Failure]. */
    class UnknownThrowable(val throwable: Throwable) :
        IllegalArgumentException("Cannot map $throwable to Result.Failure")
}