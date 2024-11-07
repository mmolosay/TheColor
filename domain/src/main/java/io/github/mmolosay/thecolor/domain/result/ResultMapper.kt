package io.github.mmolosay.thecolor.domain.result

import kotlinx.coroutines.CancellationException
import javax.inject.Inject
import kotlin.Result as KotlinResult

/**
 * Maps [KotlinResult] to domain [Result].
 */
class ResultMapper @Inject constructor(
    private val failureFactory: FailureFactory,
) {

    @Throws(IllegalStateException::class)
    fun <T> KotlinResult<T>.toDomainResult(): Result<T> =
        this.fold(
            onSuccess = { value ->
                Result.Success(value)
            },
            onFailure = { throwable ->
                if (throwable is CancellationException) throw throwable
                with(failureFactory) { throwable.asFailureOrNull() }
                    ?: error("Cannot map $throwable to Result.Failure")
            },
        )
}