package io.github.mmolosay.thecolor.domain.usecase

import io.github.mmolosay.thecolor.domain.model.BuildType

/**
 * Returns the current [BuildType] of the application.
 */
interface GetAppBuildTypeUseCase {
    operator fun invoke(): BuildType
}