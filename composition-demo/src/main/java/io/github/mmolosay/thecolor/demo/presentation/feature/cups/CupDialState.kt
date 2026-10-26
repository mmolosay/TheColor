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

// TODO: shouldn't be declared here. Probably should be a variable (constructor-injected, maybe with assisted injection)
//       parameter inside the Environment. It should also be present in the State so that ViewData has the upper limit
//       for the presentational purposes, or make the View work without a real upper bound if it's possible.
// the dial's end, in cups; a larger amount shows there
internal const val MaxCups = 4f
