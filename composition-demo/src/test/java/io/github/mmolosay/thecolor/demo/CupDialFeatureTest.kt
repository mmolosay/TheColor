package io.github.mmolosay.thecolor.demo

import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.demo.domain.Amount
import io.github.mmolosay.thecolor.demo.domain.AmountConverter
import io.github.mmolosay.thecolor.demo.domain.CupSampler
import io.github.mmolosay.thecolor.demo.presentation.feature.cups.CupDialEnvironment
import io.github.mmolosay.thecolor.demo.presentation.feature.cups.CupDialFeature
import io.github.mmolosay.thecolor.demo.presentation.feature.cups.CupDialState
import io.github.mmolosay.thecolor.demo.presentation.feature.cups.CupDialStateFactory
import io.github.mmolosay.thecolor.demo.presentation.feature.cups.withAmount
import io.github.mmolosay.thecolor.demo.presentation.format.LocaleAmountFormatter
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

/**
 * Its work runs on a [StandardTestDispatcher], so that a sample can be on its way to the store as
 * the amount is set from elsewhere, before the work is stopped: as on a device, when both arrive at
 * once.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CupDialFeatureTest {

    val testDispatcher = StandardTestDispatcher()

    val environment = CupDialEnvironment(
        amountConverter = AmountConverter(),
        amountFormatter = LocaleAmountFormatter(),
    )

    val sampler: CupSampler = mockk()

    lateinit var store: Store<CupDialState, Nothing>

    lateinit var sut: CupDialFeature

    @Test
    fun `given a sample of the dragged position is being worked out, when the parent sets an amount as the sample arrives, then the parent's amount stays, and a sample from a drag that goes on applies`() =
        runTest(testDispatcher) {
            // GIVEN
            val firstSample = CompletableDeferred<Amount.Cups>()
            coEvery { sampler.amountAt(1f) } coAnswers { firstSample.await() }
            coEvery { sampler.amountAt(2f) } returns Amount.Cups(whole = 2, numerator = 0, denominator = 4)
            createSut(scope = backgroundScope)
            sut.drag(position = 1f)
            runCurrent() // the sample of 1 cup starts

            // WHEN-THEN #1
            firstSample.complete(Amount.Cups(whole = 1, numerator = 0, denominator = 4))
            setAmount(Amount.Grams(value = 60))
            runCurrent()
            store.value.amount shouldBe Amount.Grams(value = 60)

            // WHEN-THEN #2
            sut.drag(position = 2f)
            runCurrent()
            store.value.amount shouldBe Amount.Cups(whole = 2, numerator = 0, denominator = 4)
        }

    fun createSut(scope: CoroutineScope) {
        val state = context(environment) { CupDialStateFactory.create(amount = null) }
        store = Store(initialState = state)
        sut = CupDialFeature(
            scope = scope,
            store = store,
            environment = environment,
            sampler = sampler,
            workDispatcher = testDispatcher,
        )
    }

    // as the parent does, through the feature's operation
    fun setAmount(amount: Amount) {
        store.update { dial ->
            context(environment) { Change(dial.withAmount(amount)) }
        }
    }
}
