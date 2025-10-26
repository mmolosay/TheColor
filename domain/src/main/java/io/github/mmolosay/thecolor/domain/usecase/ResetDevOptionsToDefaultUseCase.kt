package io.github.mmolosay.thecolor.domain.usecase

import io.github.mmolosay.thecolor.domain.repository.DefaultDevOptions

/**
 * Resets all values of "Developer Options" feature to their default (production) values specified in [DefaultDevOptions].
 *
 * It is an interface, because the actual implementation is powered by an external library
 * (implementation of database), thus is implemented in Data architectural layer.
 */
interface ResetDevOptionsToDefaultUseCase {
    suspend operator fun invoke()
}