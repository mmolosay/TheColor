package io.github.mmolosay.thecolor.demo.presentation.feature.sheet

import io.github.mmolosay.thecolor.composition.Token

// no nutrition: its Nutrition's View shows it, from the card's own data
data class SubstituteSheetViewData(
    val session: Token?, // the open session, which the View shows a sheet of its own for; none while closed
)
