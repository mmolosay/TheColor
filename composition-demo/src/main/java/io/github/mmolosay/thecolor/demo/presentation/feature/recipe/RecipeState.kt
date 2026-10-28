package io.github.mmolosay.thecolor.demo.presentation.feature.recipe

import io.github.mmolosay.thecolor.demo.domain.Amount
import io.github.mmolosay.thecolor.demo.domain.Sort
import io.github.mmolosay.thecolor.demo.presentation.feature.editor.AmountEditorState
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.NutritionState
import io.github.mmolosay.thecolor.demo.presentation.feature.sheet.SubstituteSheetState
import io.github.mmolosay.thecolor.demo.presentation.feature.substitutes.SubstitutesState
import io.github.mmolosay.thecolor.utils.Identity
import io.github.mmolosay.thecolor.utils.Lens

// The recipe's amount of flour, the nutrition card and the substitutes for it, and the sheet for a
// substitute
data class RecipeState(
    val editor: AmountEditorState,
    val nutrition: NutritionState,
    val substitutes: SubstitutesState,
    val sheet: SubstituteSheetState,
    val load: LoadRequest?, // the saved amount, to fill the editor with; see awaitedLoad
    val save: SaveRequest?, // the last whole amount submitted, to save; see awaitedSave
    val messages: List<SavedMessage>, // oldest first
)

object RecipeStateLenses {

    val editor = Lens<RecipeState, AmountEditorState>(
        get = { state -> state.editor },
        set = { state, editor -> state.copy(editor = editor) },
    )

    val nutrition = Lens<RecipeState, NutritionState>(
        get = { state -> state.nutrition },
        set = { state, nutrition -> state.copy(nutrition = nutrition) },
    )

    val substitutes = Lens<RecipeState, SubstitutesState>(
        get = { state -> state.substitutes },
        set = { state, substitutes -> state.copy(substitutes = substitutes) },
    )

    val sheet = Lens<RecipeState, SubstituteSheetState>(
        get = { state -> state.sheet },
        set = { state, sheet -> state.copy(sheet = sheet) },
    )
}

data class LoadRequest(
    val editorVersion: Int, // the editor's, when the load started
    val identity: Identity = Identity(),
)

data class SaveRequest(
    val amount: Amount,
    val identity: Identity = Identity(),
)

// "Saved 120 g"
data class SavedMessage(
    val amount: Amount,
    val identity: Identity = Identity(),
)

// a preset of the amount and the substitutes' sort, set together
enum class RecipePreset(
    val amount: Amount,
    val sort: Sort,
) {
    CupByProtein(
        amount = Amount.Cups(whole = 1, numerator = 0, denominator = 1),
        sort = Sort.Protein,
    ),
    HalfCupByCalories(
        amount = Amount.Cups(whole = 0, numerator = 1, denominator = 2),
        sort = Sort.Calories,
    ),
}
