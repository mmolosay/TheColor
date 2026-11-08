package io.github.mmolosay.thecolor.demo.data

import io.github.mmolosay.thecolor.demo.domain.Amount
import io.github.mmolosay.thecolor.demo.domain.CupSampler
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

// rounds to the nearest quarter cup, slowly enough that a drag outruns it, and quickly enough that
// samples land many times a second
class FakeCupSampler @Inject constructor() : CupSampler {

    override suspend fun amountAt(position: Float): Amount.Cups {
        delay(SampleTime)
        val quarters = (position * Quarter).roundToInt()
        return Amount.Cups(
            whole = quarters / Quarter,
            numerator = quarters % Quarter,
            denominator = Quarter,
        )
    }

    private companion object {
        const val Quarter = 4
        val SampleTime = 40.milliseconds
    }
}
