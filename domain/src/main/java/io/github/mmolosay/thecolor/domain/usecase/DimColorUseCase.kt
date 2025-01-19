package io.github.mmolosay.thecolor.domain.usecase

import io.github.mmolosay.thecolor.domain.model.Color

/**
 * It is an interface, because the actual implementation is powered by an external library,
 * thus is implemented in Data architectural layer.
 */
interface DimColorUseCase {
    fun Color.dim(desiredLightness: Float = 0.40f): Color
}