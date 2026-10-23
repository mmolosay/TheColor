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
import io.github.mmolosay.thecolor.demo.presentation.feature.sheet.SubstituteSheetEnvironment
import io.github.mmolosay.thecolor.demo.presentation.feature.sheet.SubstituteSheetFeature
import io.github.mmolosay.thecolor.demo.presentation.feature.sheet.SubstituteSheetState
import io.github.mmolosay.thecolor.demo.presentation.feature.sheet.SubstituteSheetStateFactory
import io.github.mmolosay.thecolor.demo.presentation.feature.sheet.withSubstitute
import io.github.mmolosay.thecolor.demo.presentation.format.LocaleAmountFormatter
import io.github.mmolosay.thecolor.demo.presentation.format.LocaleNutritionFormatter
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class SubstituteSheetFeatureTest {

    val environment = SubstituteSheetEnvironment(
        nutrition = NutritionEnvironment(
            amountConverter = AmountConverter(),
            amountFormatter = LocaleAmountFormatter(),
            nutritionFormatter = LocaleNutritionFormatter(),
        ),
    )

    // never called: the sheet opens with facts its parent already has
    val nutritionService: NutritionService = mockk()

    lateinit var store: Store<SubstituteSheetState, Nothing>

    lateinit var sut: SubstituteSheetFeature

    @Test
    fun `given the user has just closed the sheet, when the parent opens it again for the same substitute, then its view data differs from what the View showed before, so that the View can't take it for the sheet the user dismissed`() {
        createSut()
        open(substitute)
        val shown = sut.viewData.value
        sut.close(shown.session.shouldNotBeNull())

        open(substitute)

        sut.viewData.value shouldNotBe shown
    }

    fun createSut() {
        val state = context(environment) { SubstituteSheetStateFactory.create() }
        // called at once, inside each commit, as on the main thread
        store = Store(initialState = state, main = Dispatchers.Unconfined)
        sut = SubstituteSheetFeature(
            lifetime = CoroutineScope(Job() + Dispatchers.Unconfined),
            store = store,
            environment = environment,
            nutritionFactory = nutritionFactory,
        )
    }

    // as the parent does, through the feature's operation
    fun open(facts: NutritionFacts) {
        store.update { sheet ->
            context(environment) { Change(sheet.withSubstitute(facts)) }
        }
    }

    val nutritionFactory = NutritionFeature.Factory { lifetime, store, environment ->
        NutritionFeature(lifetime, store, environment, nutritionService)
    }

    val substitute = NutritionFacts(
        ingredient = Ingredient(name = "Oat flour"),
        amount = Amount.Grams(value = 120),
        calories = 485,
        protein = BigDecimal("17.6"),
        suggestedAmount = Amount.Grams(value = 120),
    )
}
