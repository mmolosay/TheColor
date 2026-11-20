package io.github.mmolosay.thecolor.presentation.devoptions.viewmodel

import io.github.mmolosay.thecolor.domain.dev.options.DevOptions.HttpLogging as DomainHttpLogging
import io.github.mmolosay.thecolor.domain.dev.options.DevOptions.PredictableRandomColors as DomainPredictableRandomColors
import io.github.mmolosay.thecolor.domain.dev.options.DevOptions.StrictMode as DomainStrictMode

data class DevOptionsState(
    val predictableRandomColors: DomainPredictableRandomColors?,
    val strictMode: DomainStrictMode?,
    val httpLogging: DomainHttpLogging?,
)