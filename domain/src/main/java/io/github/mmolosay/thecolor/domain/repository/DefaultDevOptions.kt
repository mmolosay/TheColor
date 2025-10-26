package io.github.mmolosay.thecolor.domain.repository

import io.github.mmolosay.thecolor.domain.model.DevOptions

/**
 * Stores default values of options of "developer options" feature.
 * They are used when there's no user-overridden value defined.
 */
object DefaultDevOptions {

    val PredictableRandomColors: DevOptions.PredictableRandomColors =
        DevOptions.PredictableRandomColors.Random
}