package io.github.mmolosay.thecolor.domain.exception

import io.github.mmolosay.thecolor.utils.mapFailure
import kotlinx.coroutines.CancellationException

fun <T> Result<T>.tryMapFailureToDomain(
    domainFailureFactory: DomainFailureFactory,
): Result<T> =
    this.mapFailure { exception ->
        if (exception is CancellationException) throw exception // re-throw to propagate normal cancellation
        val domainFailure = with(domainFailureFactory) { exception.toDomainFailureOrNull() }
        if (domainFailure != null) {
            DomainException(failure = domainFailure, cause = exception)
        } else {
            exception
        }
    }