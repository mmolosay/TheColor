package io.github.mmolosay.thecolor.domain.exception

/**
 * Exception representing a common failure in the Domain layer.
 * Associates a [DomainFailure] with its underlying [cause].
 *
 * Widely used to propagate a [DomainFailure] via Kotlin's [Result].
 */
data class DomainException(
    val failure: DomainFailure,
    override val cause: Throwable,
) : RuntimeException(cause)

/**
 * Classification of common failures that may occur in the Domain layer.
 * Represents the domain-level meaning of a failure independently of the scenario that caused it.
 *
 * Only common, cross-feature, app-wide failures should be defined as [DomainFailure].
 * Use-case or feature-specific failures should be defined in their respective components instead.
 */
sealed interface DomainFailure {

    sealed interface Http : DomainFailure {

        data object UnknownHost : Http

        data object Timeout : Http

        data object IO : Http

        data class ErrorResponse(
            val httpCode: Int,
            val httpMessage: String,
        ) : Http
    }
}