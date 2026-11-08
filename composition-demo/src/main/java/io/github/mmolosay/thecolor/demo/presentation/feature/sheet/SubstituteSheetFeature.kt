package io.github.mmolosay.thecolor.demo.presentation.feature.sheet

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.composition.CompositionUtils.child
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.NutritionFeature
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.NutritionOutcome
import io.github.mmolosay.thecolor.utils.mapState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import io.github.mmolosay.thecolor.demo.presentation.feature.sheet.SubstituteSheetStateLenses as StateLenses

class SubstituteSheetFeature @AssistedInject constructor(
    @Assisted private val core: SubstituteSheetCore,
    @Assisted private val store: Store<SubstituteSheetState, *>,
    @Assisted scope: CoroutineScope,
    nutritionFactory: NutritionFeature.Factory,
) {

    val viewData: StateFlow<SubstituteSheetViewData> =
        store.mapState { state -> with(core) { state.toViewData() } }

    // for its View, which shows the card's View
    val nutrition: NutritionFeature = run {
        // A suggestion taken here moves only the sheet's card, which the card does itself; the
        // recipe's amount stays. So there's nothing more to decide
        val slice = store.slice(StateLenses.nutrition) { state, _: NutritionOutcome? ->
            Change(state)
        }
        nutritionFactory.create(
            core = core.nutritionCore,
            store = slice,
            scope = scope.child(),
        )
    }

    fun close(session: SheetSession) {
        store.update { state -> with(core) { state.close(session) } }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            core: SubstituteSheetCore,
            store: Store<SubstituteSheetState, *>,
            scope: CoroutineScope,
        ): SubstituteSheetFeature
    }
}
