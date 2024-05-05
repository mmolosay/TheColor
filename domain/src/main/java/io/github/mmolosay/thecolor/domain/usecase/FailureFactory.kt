package io.github.mmolosay.thecolor.domain.usecase

import io.github.mmolosay.thecolor.domain.failure.Failure
import io.github.mmolosay.thecolor.domain.failure.HttpFailure
import javax.inject.Inject

/**
 * Creates some type of [Failure] from provided [Throwable].
 * Returns `null` if this particular [Throwable] cannot be translated to any [Failure].
 */
class FailureFactory @Inject constructor(
    private val httpFailureFactory: HttpFailureFactory,
) {

    fun Throwable.asFailureOrNull(): Failure? =
        null
            ?: with(httpFailureFactory) { asHttpFailureOrNull() }
    // add other types in new line like this ^

}

interface HttpFailureFactory {
    fun Throwable.asHttpFailureOrNull(): HttpFailure?
}