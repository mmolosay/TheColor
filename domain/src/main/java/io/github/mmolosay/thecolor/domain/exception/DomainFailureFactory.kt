package io.github.mmolosay.thecolor.domain.exception

import javax.inject.Inject

/**
 * Creates some type of [DomainException] from the provided [Throwable].
 * Returns the provided [Throwable] as-is if it is already an instance of [DomainException].
 * Throws [IllegalOfferedThrowableException] if the provided [Throwable] cannot be translated.
 */
class DomainFailureFactory @Inject constructor(
    private val httpFactory: HttpDomainFailureFactory,
) {

    @Suppress("USELESS_ELVIS_LEFT_IS_NULL")
    fun Throwable.toDomainFailure(): DomainFailure =
        null
            ?: with(httpFactory) { toHttpDomainFailureOrNull() }
            // add other types in new line like this ^
            ?: throw IllegalOfferedThrowableException(offered = this)

    class IllegalOfferedThrowableException(val offered: Throwable) : IllegalArgumentException(
        "Offered $offered Throwable cannot be translated to DomainFailure",
    )
}

@Suppress("unused")
context(factory: DomainFailureFactory)
fun Throwable.toDomainFailureOrNull(): DomainFailure? =
    try {
        val throwable = this
        with(factory) { throwable.toDomainFailure() }
    } catch (_: DomainFailureFactory.IllegalOfferedThrowableException) {
        null
    }

interface HttpDomainFailureFactory {
    fun Throwable.toHttpDomainFailureOrNull(): DomainFailure.Http?
}