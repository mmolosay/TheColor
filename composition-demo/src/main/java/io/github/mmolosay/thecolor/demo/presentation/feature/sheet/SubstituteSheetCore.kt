package io.github.mmolosay.thecolor.demo.presentation.feature.sheet

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.demo.domain.NutritionFacts
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.NutritionCore

class SubstituteSheetCore @AssistedInject constructor(
    nutritionFactory: NutritionCore.Factory,
) {

    // its card's
    val nutritionCore: NutritionCore = nutritionFactory.create()

    // closed
    fun initialState(): SubstituteSheetState =
        SubstituteSheetState(session = null, nutrition = nutritionCore.initialState())

    // region Getters

    fun SubstituteSheetState.toViewData(): SubstituteSheetViewData =
        SubstituteSheetViewData(session = this.session)

    // endregion

    // region Setters

    // for the parent: a new session for a substitute, from the facts the parent already has
    fun SubstituteSheetState.withSubstitute(facts: NutritionFacts): SubstituteSheetState {
        val card = with(nutritionCore) { nutrition.withFacts(facts) }
        return SubstituteSheetState(session = SheetSession(), nutrition = card)
    }

    // endregion

    // region Transitions

    // For its Instance only. They can run more than once, so they must have no side effects

    // applies only to the session the View showed, so that closing an earlier session never closes
    // a newer one
    fun SubstituteSheetState.close(session: SheetSession): Change<SubstituteSheetState, Nothing> {
        if (this.session != session) return Change(this)
        val state = SubstituteSheetState(session = null, nutrition = nutritionCore.initialState())
        return Change(state)
    }

    // endregion

    @AssistedFactory
    fun interface Factory {
        fun create(): SubstituteSheetCore
    }
}
