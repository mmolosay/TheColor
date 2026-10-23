package io.github.mmolosay.thecolor.demo.presentation.feature.cups

import io.github.mmolosay.thecolor.demo.domain.Amount

// Compared by reference: a sample of the position the dial was at when it started. It stays the same
// through a drag, so that moving on doesn't restart it, and a sample always lands
class CupSampleRequest(
    val position: Float,
)

// A dial the user drags over cups. Slow work works out the amount at the position it's dragged to
data class CupDialState(
    val position: Float, // in cups: where the user dragged it, or where the amount set from elsewhere is
    val amount: Amount?, // the last sample, or the amount set from elsewhere; none for no amount
    val request: CupSampleRequest?, // the sample being worked out; none at rest
)

object CupDialStateFactory {

    // from the parent's choice of amount
    context(environment: CupDialEnvironment)
    fun create(amount: Amount?): CupDialState {
        val empty = CupDialState(position = 0f, amount = null, request = null)
        return empty.withAmount(amount)
    }
}

// the fact: the amount the dial stands for
context(_: CupDialEnvironment)
fun CupDialState.amount(): Amount? =
    this.amount

// The same fact, given. It ends the wait for a sample, which is of a position from before, so that
// its late result never undoes the amount
context(environment: CupDialEnvironment)
fun CupDialState.withAmount(amount: Amount?): CupDialState {
    val position = run {
        if (amount == null) return@run 0f
        // to a hundredth of a cup, finer than a finger drags
        val cups = with(environment.amountConverter) { amount.toCups(denominator = 100) }
        val exact = cups.whole + cups.numerator / 100f
        exact.coerceAtMost(MaxCups)
    }
    return CupDialState(position = position, amount = amount, request = null)
}

// the work the state awaits, derived from it
context(_: CupDialEnvironment)
fun CupDialState.awaited(): CupSampleRequest? =
    this.request

context(_: CupDialEnvironment)
fun CupDialState.isEmpty(): Boolean =
    (this.amount == null && this.request == null)

context(environment: CupDialEnvironment)
fun CupDialState.toViewData(): CupDialViewData {
    val amount = this.amount ?: return CupDialViewData(position = this.position, cups = null)
    val cups = with(environment.amountConverter) { amount.toCups(denominator = 4) }
    return CupDialViewData(
        position = this.position,
        cups = environment.amountFormatter.format(cups),
    )
}
