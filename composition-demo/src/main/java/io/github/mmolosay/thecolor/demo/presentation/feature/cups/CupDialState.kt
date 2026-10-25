package io.github.mmolosay.thecolor.demo.presentation.feature.cups

import io.github.mmolosay.thecolor.demo.domain.Amount
import io.github.mmolosay.thecolor.utils.Identity

// A sample of the position the dial was at when it started. It stays the same through a drag, so
// that moving on doesn't restart it, and a sample always lands, unless the amount is set from
// elsewhere meanwhile
data class CupSampleRequest(
    val position: Float,
    val amountVersion: Int, // the amount's, when the sample started
    val identity: Identity = Identity(),
)

// A dial the user drags over cups. Slow work works out the amount at the position it's dragged to
data class CupDialState(
    val position: Float, // in cups: where the user dragged it, or where the amount set from elsewhere is
    val positionVersion: Int,
    val amount: Amount?, // the last sample, or the amount set from elsewhere; none for no amount
    val amountVersion: Int,
    val request: CupSampleRequest?, // the last sample started; see awaited
)

object CupDialStateFactory {

    // from the parent's choice of amount
    context(environment: CupDialEnvironment)
    fun create(amount: Amount?): CupDialState {
        return CupDialState(
            position = positionFor(amount),
            positionVersion = 0,
            amount = amount,
            amountVersion = 0,
            request = null,
        )
    }
}

// the fact: the amount the dial stands for
context(_: CupDialEnvironment)
fun CupDialState.amount(): Amount? =
    this.amount

// The same fact, given. A sample being worked out is of a position from before: it finds the
// amount's version moved on, so its late result never undoes the amount
context(environment: CupDialEnvironment)
fun CupDialState.withAmount(amount: Amount?): CupDialState {
    val position = positionFor(amount)
    return this.withPosition(position).withAmountOnly(amount)
}

// where the dial stands for an amount, to a hundredth of a cup, finer than a finger drags
context(environment: CupDialEnvironment)
private fun positionFor(amount: Amount?): Float {
    if (amount == null) return 0f
    val cups = with(environment.amountConverter) { amount.toCups(denominator = 100) }
    val exact = cups.whole + cups.numerator / 100f
    return exact.coerceAtMost(MaxCups)
}

// The only ways the position and the amount change, so that each version moves with every change,
// whoever makes it. A value that changes nothing leaves the state as it is

context(_: CupDialEnvironment)
fun CupDialState.withPosition(position: Float): CupDialState {
    if (position == this.position) return this
    return this.copy(position = position, positionVersion = this.positionVersion + 1)
}

context(_: CupDialEnvironment)
fun CupDialState.withAmountOnly(amount: Amount?): CupDialState {
    if (amount == this.amount) return this
    return this.copy(amount = amount, amountVersion = this.amountVersion + 1)
}

// for the parent
context(_: CupDialEnvironment)
fun CupDialState.version(): Int =
    this.positionVersion + this.amountVersion

// The work the state awaits, derived from it: the last sample, while nothing has set the amount
// since it started. A drag moves only the position, so a sample lands while the drag goes on
context(_: CupDialEnvironment)
fun CupDialState.awaited(): CupSampleRequest? {
    val request = this.request ?: return null
    return if (request.amountVersion == this.amountVersion) request else null
}

context(environment: CupDialEnvironment)
fun CupDialState.toViewData(): CupDialViewData {
    val amount = this.amount ?: return CupDialViewData(position = this.position, cups = null)
    val cups = with(environment.amountConverter) { amount.toCups(denominator = 4) }
    return CupDialViewData(
        position = this.position,
        cups = environment.amountFormatter.format(cups),
    )
}
