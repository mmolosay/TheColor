package io.github.mmolosay.thecolor.domain.model

/**
 * A collection of models that represent options of "Developer Options" feature.
 */
object DevOptions {

    enum class PredictableRandomColors {
        Random,
        CyclingRgb,
        CyclingLightDark,
    }

    @JvmInline
    value class StrictMode(
        val enabled: Boolean,
    )
}