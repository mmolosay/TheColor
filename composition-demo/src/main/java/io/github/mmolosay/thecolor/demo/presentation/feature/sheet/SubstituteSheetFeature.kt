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
    @Assisted scope: CoroutineScope,
    @Assisted private val store: Store<SubstituteSheetState, *>,
    @Assisted private val core: SubstituteSheetCore,
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
            scope = scope.child(),
            store = slice,
            core = core.nutritionCore,
        )
    }

    fun close(session: SheetSession) {
        store.update { state -> with(core) { state.close(session) } }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            scope: CoroutineScope,
            store: Store<SubstituteSheetState, *>,
            core: SubstituteSheetCore,
        ): SubstituteSheetFeature
    }
}
