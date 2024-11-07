package io.github.mmolosay.thecolor.domain.usecase

/**
 * Accesses a persistent storage but retrieves no data.
 * This use case is designed to "warm up" local databases, so that subsequent accesses get
 * executed quicker.
 *
 * It is an interface, because the actual implementation is powered by an external library
 * (implementation of database), thus is implemented in Data architectural layer.
 */
interface TouchLocalDatabaseUseCase {
    suspend operator fun invoke()
}