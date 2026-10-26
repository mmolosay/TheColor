package io.github.mmolosay.thecolor.demo

import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.demo.domain.Amount
import io.github.mmolosay.thecolor.demo.domain.AmountConverter
import io.github.mmolosay.thecolor.demo.domain.Ingredient
import io.github.mmolosay.thecolor.demo.domain.NutritionFacts
import io.github.mmolosay.thecolor.demo.domain.NutritionService
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.NutritionCore
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.NutritionFeature
import io.github.mmolosay.thecolor.demo.presentation.feature.sheet.SubstituteSheetCore
import io.github.mmolosay.thecolor.demo.presentation.feature.sheet.SubstituteSheetFeature
import io.github.mmolosay.thecolor.demo.presentation.feature.sheet.SubstituteSheetState
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

    val core = SubstituteSheetCore(
        nutritionFactory = {
            NutritionCore(
                amountConverter = AmountConverter(),
                amountFormatter = LocaleAmountFormatter(),
                nutritionFormatter = LocaleNutritionFormatter(),
            )
        },
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
        store = Store(initialState = core.initialState())
        sut = SubstituteSheetFeature(
            scope = CoroutineScope(Job() + Dispatchers.Unconfined),
            store = store,
            core = core,
            nutritionFactory = nutritionFactory,
        )
    }

    // as the parent does, through the feature's operation
    fun open(facts: NutritionFacts) {
        store.update { state ->
            with(core) { Change(state.withSubstitute(facts)) }
        }
    }

    val nutritionFactory = NutritionFeature.Factory { scope, store, core ->
        NutritionFeature(scope, store, core, nutritionService)
    }

    val substitute = NutritionFacts(
        ingredient = Ingredient(name = "Oat flour"),
        amount = Amount.Grams(value = 120),
        calories = 485,
        protein = BigDecimal("17.6"),
        suggestedAmount = Amount.Grams(value = 120),
    )
}
