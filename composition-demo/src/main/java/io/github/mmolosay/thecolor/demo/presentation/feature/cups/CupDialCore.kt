package io.github.mmolosay.thecolor.demo.presentation.feature.cups

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.demo.domain.Amount
import io.github.mmolosay.thecolor.demo.domain.AmountConverter
import io.github.mmolosay.thecolor.demo.domain.AmountFormatter

class CupDialCore @AssistedInject constructor(
    private val amountConverter: AmountConverter,
    private val amountFormatter: AmountFormatter,
) {

    // from the parent's choice of amount
    fun initialState(amount: Amount?): CupDialState {
        return CupDialState(
            position = positionFor(amount),
            positionVersion = 0,
            amount = amount,
            amountVersion = 0,
            request = null,
        )
    }

    // region Getters

    // the fact: the amount the dial stands for
    fun CupDialState.amount(): Amount? =
        this.amount

    // The work the state awaits, derived from it: the last sample, while nothing has set the amount
    // since it started. A drag moves only the position, so a sample lands while the drag goes on
    fun CupDialState.awaited(): CupSampleRequest? {
        val request = this.request ?: return null
        return if (request.amountVersion == this.amountVersion) request else null
    }

    fun CupDialState.toViewData(): CupDialViewData {
        val amount = this.amount ?: return CupDialViewData(position = this.position, cups = null)
        val cups = with(amountConverter) { amount.toCups(denominator = 4) }
        return CupDialViewData(
            position = this.position,
            cups = amountFormatter.format(cups),
        )
    }

    // for the parent
    fun CupDialState.version(): Int =
        this.positionVersion + this.amountVersion

    // endregion

    // region Setters

    // The only ways the position and the amount change, so that each version moves with every
    // change, whoever makes it. A value that changes nothing leaves the state as it is

    private fun CupDialState.withPosition(position: Float): CupDialState {
        if (position == this.position) return this
        return this.copy(position = position, positionVersion = this.positionVersion + 1)
    }

    private fun CupDialState.withAmountOnly(amount: Amount?): CupDialState {
        if (amount == this.amount) return this
        return this.copy(amount = amount, amountVersion = this.amountVersion + 1)
    }

    // The same fact, given. A sample being worked out is of a position from before: it finds the
    // amount's version moved on, so its late result never undoes the amount
    fun CupDialState.withAmount(amount: Amount?): CupDialState {
        val position = positionFor(amount)
        return this.withPosition(position).withAmountOnly(amount)
    }

    // endregion

    // region Transitions

    // For its Instance only. They can run more than once, so they must have no side effects

    // the dial follows the finger at once; a sample starts unless one is being worked out already
    fun CupDialState.drag(position: Float): Change<CupDialState, Nothing> {
        val clamped = position.coerceIn(0f, MaxCups)
        if (clamped == this.position) return Change(this)
        val moved = this.withPosition(clamped)
        val request = moved.awaited() ?: run {
            CupSampleRequest(position = clamped, amountVersion = moved.amountVersion)
        }
        val state = moved.copy(request = request)
        return Change(state)
    }

    // Applies only to the sample the state still waits for. If the dial has moved on since the
    // sample started, another one starts, for where the dial is now
    fun CupDialState.receiveSample(
        request: CupSampleRequest,
        amount: Amount.Cups,
    ): Change<CupDialState, Nothing> {
        // the amount was set from elsewhere since
        if (this.awaited() != request) return Change(this)
        val sampled = this.withAmountOnly(amount)
        val next = run {
            if (sampled.position == request.position) return@run null
            CupSampleRequest(position = sampled.position, amountVersion = sampled.amountVersion)
        }
        val state = sampled.copy(request = next)
        return Change(state)
    }

    // endregion

    // where the dial stands for an amount, to a hundredth of a cup, finer than a finger drags
    private fun positionFor(amount: Amount?): Float {
        if (amount == null) return 0f
        val cups = with(amountConverter) { amount.toCups(denominator = 100) }
        val exact = cups.whole + cups.numerator / 100f
        return exact.coerceAtMost(MaxCups)
    }

    @AssistedFactory
    fun interface Factory {
        fun create(): CupDialCore
    }
}
