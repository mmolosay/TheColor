package io.github.mmolosay.thecolor.demo.presentation.feature.sheet

import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.NutritionState
import io.github.mmolosay.thecolor.utils.Identity
import io.github.mmolosay.thecolor.utils.Lens

// one opening of the sheet
data class SheetSession(
    val identity: Identity = Identity(),
)

// A substitute's nutrition, opened from the substitutes. Each opening is a session of its own, even
// for the same substitute, so that a sheet opened again is never taken for one the user closed
data class SubstituteSheetState(
    val session: SheetSession?, // none while closed
    val nutrition: NutritionState,
)

object SubstituteSheetStateLenses {

    val nutrition = Lens<SubstituteSheetState, NutritionState>(
        get = { state -> state.nutrition },
        set = { state, nutrition -> state.copy(nutrition = nutrition) },
    )
}
