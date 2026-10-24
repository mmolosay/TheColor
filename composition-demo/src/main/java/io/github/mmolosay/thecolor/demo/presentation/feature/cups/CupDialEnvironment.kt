package io.github.mmolosay.thecolor.demo.presentation.feature.cups

import io.github.mmolosay.thecolor.demo.domain.AmountConverter
import io.github.mmolosay.thecolor.demo.domain.AmountFormatter
import javax.inject.Inject

class CupDialEnvironment @Inject constructor(
    val amountConverter: AmountConverter,
    val amountFormatter: AmountFormatter,
)

// TODO: shouldn't be declared here. Probably should be a variable (constructor-injected, maybe with assisted injection)
//       parameter inside the Environment. It should also be present in the State so that ViewData has the upper limit
//       for the presentational purposes, or make the View work without a real upper bound if it's possible.
// the dial's end, in cups; a larger amount shows there
internal const val MaxCups = 4f
