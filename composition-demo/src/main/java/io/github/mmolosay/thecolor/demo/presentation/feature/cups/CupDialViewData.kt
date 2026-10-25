package io.github.mmolosay.thecolor.demo.presentation.feature.cups

data class CupDialViewData(
    val position: Float, // in cups, from none to MaxCups
    val cups: String?, // "1 2/4 cups", to the nearest quarter; none for no amount
)
