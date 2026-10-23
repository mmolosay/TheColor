package io.github.mmolosay.thecolor.demo.presentation.feature.sheet

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.composition.CompositionUtils.child
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.composition.Token
import io.github.mmolosay.thecolor.composition.viewData
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.NutritionFeature
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.NutritionOutcome
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.NutritionState
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.NutritionStateFactory
import io.github.mmolosay.thecolor.utils.Lens
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class SubstituteSheetFeature @AssistedInject constructor(
    @Assisted lifetime: CoroutineScope,
    @Assisted private val store: Store<SubstituteSheetState, *>,
    @Assisted private val environment: SubstituteSheetEnvironment,
    nutritionFactory: NutritionFeature.Factory,
) {

    val viewData: StateFlow<SubstituteSheetViewData> =
        store.viewData { sheet -> context(environment) { sheet.toViewData() } }

    // for its View, which shows the card's View
    val nutrition: NutritionFeature = run {
        // A suggestion taken here moves only the sheet's card, which the card does itself; the
        // recipe's amount stays. So there's nothing more to decide
        val slice = store.slice<NutritionState, NutritionOutcome>(nutritionLens) { sheet, _ ->
            Change(sheet)
        }
        nutritionFactory.create(
            lifetime = lifetime.child(),
            store = slice,
            environment = environment.nutrition,
        )
    }

    fun close(session: Token) {
        store.update { sheet ->
            context(environment) { sheet.close(session) }
        }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            lifetime: CoroutineScope,
            store: Store<SubstituteSheetState, *>,
            environment: SubstituteSheetEnvironment,
        ): SubstituteSheetFeature
    }
}

private val nutritionLens = Lens<SubstituteSheetState, NutritionState>(
    get = { sheet -> sheet.nutrition },
    set = { sheet, nutrition -> sheet.copy(nutrition = nutrition) },
)

// The transitions behind the actions. Only the feature instance calls them. They're outside the
// class so that they can't reach its lifetime or store: they can run more than once, so they must
// have no side effects

// applies only to the session the View showed, so that closing an earlier session never closes a
// newer one
context(environment: SubstituteSheetEnvironment)
fun SubstituteSheetState.close(session: Token): Change<SubstituteSheetState, Nothing> {
    if (this.session !== session) return Change(this)
    val nutrition = context(environment.nutrition) { NutritionStateFactory.create() }
    val state = SubstituteSheetState(session = null, nutrition = nutrition)
    return Change(state)
}
