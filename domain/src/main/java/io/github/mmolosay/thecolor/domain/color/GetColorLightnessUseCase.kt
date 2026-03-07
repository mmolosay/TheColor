package io.github.mmolosay.thecolor.domain.color

/**
 * It is an interface, because the actual implementation is powered by an external library,
 * thus is implemented in Data architectural layer.
 */
interface GetColorLightnessUseCase {
    fun Color.labLightness(): Float
}