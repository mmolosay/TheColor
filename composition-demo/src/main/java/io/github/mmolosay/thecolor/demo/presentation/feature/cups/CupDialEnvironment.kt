package io.github.mmolosay.thecolor.demo.presentation.feature.cups

import io.github.mmolosay.thecolor.demo.domain.AmountConverter
import io.github.mmolosay.thecolor.demo.domain.AmountFormatter
import javax.inject.Inject

class CupDialEnvironment @Inject constructor(
    val amountConverter: AmountConverter,
    val amountFormatter: AmountFormatter,
)

// the dial's end, in cups; a larger amount shows there
internal const val MaxCups = 4f
