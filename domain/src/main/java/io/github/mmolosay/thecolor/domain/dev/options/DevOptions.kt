package io.github.mmolosay.thecolor.domain.dev.options

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

    @JvmInline
    value class HttpLogging(
        val enabled: Boolean,
    )
}