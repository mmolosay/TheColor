package io.github.mmolosay.thecolor.demo.domain

// Works out the amount at a position of the cup dial, in cups, as a whole number of quarter cups.
// It takes a moment, as sampling a color area does
interface CupSampler {
    suspend fun amountAt(position: Float): Amount.Cups
}
