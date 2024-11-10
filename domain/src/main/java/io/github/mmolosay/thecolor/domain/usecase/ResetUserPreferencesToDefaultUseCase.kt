package io.github.mmolosay.thecolor.domain.usecase

import io.github.mmolosay.thecolor.domain.repository.DefaultUserPreferences

/**
 * Resets user-configured preferences to their default values specified in [DefaultUserPreferences].
 *
 * It is an interface, because the actual implementation is powered by an external library
 * (implementation of database), thus is implemented in Data architectural layer.
 */
interface ResetUserPreferencesToDefaultUseCase {
    suspend operator fun invoke()
}