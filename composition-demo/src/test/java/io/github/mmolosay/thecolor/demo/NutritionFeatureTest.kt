package io.github.mmolosay.thecolor.demo

import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.demo.domain.Amount
import io.github.mmolosay.thecolor.demo.domain.AmountConverter
import io.github.mmolosay.thecolor.demo.domain.Ingredient
import io.github.mmolosay.thecolor.demo.domain.NutritionFacts
import io.github.mmolosay.thecolor.demo.domain.NutritionService
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.NutritionEnvironment
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.NutritionFeature
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.NutritionOutcome
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.NutritionState
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.NutritionStateFactory
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.NutritionViewData
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.forAmountOf
import io.github.mmolosay.thecolor.demo.presentation.format.LocaleAmountFormatter
import io.github.mmolosay.thecolor.demo.presentation.format.LocaleNutritionFormatter
import io.github.mmolosay.thecolor.utils.Lens
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.io.IOException
import java.math.BigDecimal

/**
 * Tests [NutritionFeature] on a store of its own, with the parent's operation called on that store.
 *
 * Its work runs on a [StandardTestDispatcher], so that a result can be on its way to the store as
 * the amount changes, before the work for the old amount is stopped: as on a device, when a result
 * and the user's edit arrive at once.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NutritionFeatureTest {

    val testDispatcher = StandardTestDispatcher()

    // real implementations: the view data says which amount the card is for
    val environment = NutritionEnvironment(
        amountConverter = AmountConverter(),
        amountFormatter = LocaleAmountFormatter(),
        nutritionFormatter = LocaleNutritionFormatter(),
    )

    val service: NutritionService = mockk()

    lateinit var store: Store<NutritionState, Nothing>

    lateinit var sut: NutritionFeature

    @Test
    fun `given the facts for 12 g are loading, when they arrive as the parent sets 120 g, then they're dropped and 120 g shows as loading`() =
        runTest(testDispatcher) {
            val factsFor12Grams = CompletableDeferred<Result<NutritionFacts>>()
            coEvery { service.nutritionFacts(Ingredient.Flour, Amount.Grams(value = 12)) } coAnswers {
                factsFor12Grams.await()
            }
            coEvery { service.nutritionFacts(Ingredient.Flour, Amount.Grams(value = 120)) } coAnswers {
                awaitCancellation() // doesn't answer within the test
            }
            createSut(lifetime = backgroundScope)
            setAmount(Amount.Grams(value = 12))
            runCurrent() // the work for 12 g starts

            factsFor12Grams.complete(Result.success(facts(grams = 12)))
            setAmount(Amount.Grams(value = 120))
            runCurrent()

            sut.viewData.value shouldBe NutritionViewData.Loading(portion = portion("120 g"))
        }

    @Test
    fun `given the facts for 13 g are loading, when the parent sets 130 g, then 13 g again, as the first request for 13 g fails, then 13 g still shows as loading, so that the old request's failure doesn't end the new one`() =
        runTest(testDispatcher) {
            val firstFactsFor13Grams = CompletableDeferred<Result<NutritionFacts>>()
            coEvery { service.nutritionFacts(Ingredient.Flour, Amount.Grams(value = 13)) } coAnswers {
                firstFactsFor13Grams.await()
            } coAndThen {
                awaitCancellation() // doesn't answer within the test
            }
            createSut(lifetime = backgroundScope)
            setAmount(Amount.Grams(value = 13))
            runCurrent() // the first work for 13 g starts

            firstFactsFor13Grams.complete(Result.failure(IOException()))
            setAmount(Amount.Grams(value = 130))
            setAmount(Amount.Grams(value = 13))
            runCurrent()

            sut.viewData.value shouldBe NutritionViewData.Loading(portion = portion("13 g"))
        }

    fun createSut(lifetime: CoroutineScope) {
        val state = context(environment) { NutritionStateFactory.create() }
        store = Store(initialState = state, main = Dispatchers.Unconfined) // no View listens
        // the parent's part: it hands nothing up
        val slice = run {
            val lens = Lens<NutritionState, NutritionState>(
                get = { nutrition -> nutrition },
                set = { _, nutrition -> nutrition },
            )
            store.slice<NutritionState, NutritionOutcome>(lens) { nutrition, _ -> Change(nutrition) }
        }
        sut = NutritionFeature(
            lifetime = lifetime,
            store = slice,
            environment = environment,
            service = service,
        )
    }

    // as the parent does, through the feature's operation
    fun setAmount(amount: Amount) {
        store.update { nutrition ->
            context(environment) { Change(nutrition.forAmountOf(Ingredient.Flour, amount)) }
        }
    }

    fun facts(grams: Int) =
        NutritionFacts(
            ingredient = Ingredient.Flour,
            amount = Amount.Grams(value = grams),
            calories = 44,
            protein = BigDecimal("1.2"),
            suggestedAmount = Amount.Grams(value = 60),
        )

    fun portion(amount: String) =
        NutritionViewData.Portion(ingredient = "Flour", amount = amount)
}
