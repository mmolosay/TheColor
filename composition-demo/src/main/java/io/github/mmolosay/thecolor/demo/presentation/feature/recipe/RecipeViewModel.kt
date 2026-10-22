package io.github.mmolosay.thecolor.demo.presentation.feature.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.demo.presentation.feature.editor.AmountEditorFeature
import io.github.mmolosay.thecolor.demo.presentation.feature.editor.AmountEditorState
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.NutritionFeature
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.NutritionState
import io.github.mmolosay.thecolor.utils.Lens
import javax.inject.Inject

@HiltViewModel
class RecipeViewModel @Inject constructor(
    environment: RecipeEnvironment,
    editorFactory: AmountEditorFeature.Factory,
    nutritionFactory: NutritionFeature.Factory,
) : ViewModel() {

    private val store = run {
        val state = context(environment) { RecipeStateFactory.create() }
        Store(initialState = state)
    }

    // for its View, which shows the editor's View
    val editor: AmountEditorFeature = run {
        val slice = store.slice(editorLens) { recipe, _: Nothing? ->
            context(environment) { onEditor(recipe) }
        }
        editorFactory.create(
            lifetime = viewModelScope,
            store = slice,
            environment = environment.editor,
        )
    }

    val nutrition: NutritionFeature = run {
        // nothing to decide: the card has no outcomes, and the screen sets its amount in onEditor
        val slice = store.slice(nutritionLens) { recipe, _: Nothing? -> Change(recipe) }
        nutritionFactory.create(
            lifetime = viewModelScope,
            store = slice,
            environment = environment.nutrition,
        )
    }
}

private val editorLens = Lens<RecipeState, AmountEditorState>(
    get = { recipe -> recipe.editor },
    set = { recipe, editor -> recipe.copy(editor = editor) },
)

private val nutritionLens = Lens<RecipeState, NutritionState>(
    get = { recipe -> recipe.nutrition },
    set = { recipe, nutrition -> recipe.copy(nutrition = nutrition) },
)

// The reducer for the editor. Only the screen's ViewModel runs it, inside the editor's update: the
// nutrition card follows the amount the edit leaves
context(_: RecipeEnvironment)
fun onEditor(recipe: RecipeState): Change<RecipeState, Nothing> {
    val state = recipe.withNutritionForAmount()
    return Change(state)
}
