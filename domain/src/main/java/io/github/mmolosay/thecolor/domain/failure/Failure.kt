package io.github.mmolosay.thecolor.domain.failure

/**
 * Represents some type of failure that has occurred during execution.
 * See derivatives for more narrow-scoped, specific subtypes.
 */
sealed interface Failure {
    val cause: Throwable
}