package io.github.mmolosay.thecolor.demo.presentation.feature.sheet

import io.github.mmolosay.thecolor.demo.domain.NutritionFacts
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.NutritionState
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.NutritionStateFactory
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.withFacts

// compared by reference: one opening of the sheet
class SheetSession

// A substitute's nutrition, in a sheet over the screen. Each opening is a session of its own, even
// for the same substitute, so that a sheet opened again is never taken for one the user closed
data class SubstituteSheetState(
    val session: SheetSession?, // none while closed
    val nutrition: NutritionState,
)

object SubstituteSheetStateFactory {

    // closed
    context(environment: SubstituteSheetEnvironment)
    fun create(): SubstituteSheetState {
        val nutrition = context(environment.nutrition) { NutritionStateFactory.create() }
        return SubstituteSheetState(session = null, nutrition = nutrition)
    }
}

// for the parent: a new session for a substitute, from the facts the parent already has
context(environment: SubstituteSheetEnvironment)
fun SubstituteSheetState.withSubstitute(facts: NutritionFacts): SubstituteSheetState {
    val nutrition = context(environment.nutrition) { this.nutrition.withFacts(facts) }
    return SubstituteSheetState(session = SheetSession(), nutrition = nutrition)
}

context(_: SubstituteSheetEnvironment)
fun SubstituteSheetState.toViewData(): SubstituteSheetViewData =
    SubstituteSheetViewData(session = this.session)
