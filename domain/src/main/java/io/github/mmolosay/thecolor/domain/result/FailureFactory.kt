package io.github.mmolosay.thecolor.domain.result

import javax.inject.Inject

/**
 * Creates some type of [Result.Failure] from provided [Throwable].
 * Returns `null` if this particular [Throwable] cannot be translated to any [Failure][Result.Failure].
 */
class FailureFactory @Inject constructor(
    private val httpFailureFactory: HttpFailureFactory,
) {

    fun Throwable.asFailureOrNull(): Result.Failure? =
        null
            ?: with(httpFailureFactory) { asHttpFailureOrNull() }
    // add other types in new line like this ^

}

interface HttpFailureFactory {
    fun Throwable.asHttpFailureOrNull(): HttpFailure?
}