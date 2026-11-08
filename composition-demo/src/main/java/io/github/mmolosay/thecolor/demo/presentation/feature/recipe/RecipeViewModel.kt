package io.github.mmolosay.thecolor.demo.presentation.feature.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.composition.CompositionUtils.serve
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.composition.slice
import io.github.mmolosay.thecolor.demo.domain.RecipeRepository
import io.github.mmolosay.thecolor.demo.presentation.feature.editor.AmountEditorFeature
import io.github.mmolosay.thecolor.demo.presentation.feature.editor.AmountEditorOutcome
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.NutritionFeature
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.NutritionOutcome
import io.github.mmolosay.thecolor.demo.presentation.feature.sheet.SubstituteSheetFeature
import io.github.mmolosay.thecolor.demo.presentation.feature.substitutes.SubstitutesFeature
import io.github.mmolosay.thecolor.demo.presentation.feature.substitutes.SubstitutesOutcome
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextFieldFeature
import io.github.mmolosay.thecolor.utils.mapState
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import io.github.mmolosay.thecolor.demo.presentation.feature.recipe.RecipeStateLenses as StateLenses

@HiltViewModel
class RecipeViewModel @Inject constructor(
    coreFactory: RecipeCore.Factory,
    editorFactory: AmountEditorFeature.Factory,
    nutritionFactory: NutritionFeature.Factory,
    substitutesFactory: SubstitutesFeature.Factory,
    sheetFactory: SubstituteSheetFeature.Factory,
    textFieldFactory: TextFieldFeature.Factory,
    private val repository: RecipeRepository, // from dependency injection: only the work uses it
) : ViewModel() {

    // the screen's, which creates its children's, and so on down the tree
    private val core: RecipeCore = coreFactory.create()

    private val store = Store(initialState = core.initialState())

    val viewData: StateFlow<RecipeViewData> =
        store.mapState { state -> with(core) { state.toViewData() } }

    // for its View, which shows the children's Views
    val editor: AmountEditorFeature = editorFactory.create(
        core = core.editorCore,
        store = store.slice(StateLenses.editor) { state, outcome: AmountEditorOutcome? ->
            with(core) { state.onEditor(outcome) }
        },
        scope = viewModelScope,
    )

    val nutrition: NutritionFeature = nutritionFactory.create(
        core = core.nutritionCore,
        store = store.slice(StateLenses.nutrition) { state, outcome: NutritionOutcome? ->
            with(core) { state.onNutrition(outcome) }
        },
        scope = viewModelScope,
    )

    val substitutes: SubstitutesFeature = substitutesFactory.create(
        core = core.substitutesCore,
        store = store.slice(StateLenses.substitutes) { state, outcome: SubstitutesOutcome? ->
            with(core) { state.onSubstitutes(outcome) }
        },
        scope = viewModelScope,
    )

    // nothing to decide: the sheet has no outcomes, and the screen opens it in onSubstitutes
    val sheet: SubstituteSheetFeature = sheetFactory.create(
        core = core.sheetCore,
        store = store.slice(StateLenses.sheet) { state -> Change(state) },
        scope = viewModelScope,
    )

    // Nothing in the screen's functions reads or writes the servings, so the field gets a store of its
    // own, and its commits wake only its own View
    val servings: TextFieldFeature = textFieldFactory.create(
        core = core.servingsCore,
        store = Store(initialState = core.servingsCore.initialState(text = "1")),
        scope = viewModelScope,
    )

    init {
        // each kind of work on its own, so that one never restarts the other
        serveLoad()
        serveSave()
    }

    private fun serveLoad() {
        viewModelScope.serve(
            store = store,
            request = { state -> with(core) { state.awaitedLoad() } },
        ) { request ->
            val amount = repository.lastAmount()
            store.update { state -> with(core) { state.completeLoad(amount, request) } }
        }
    }

    private fun serveSave() {
        viewModelScope.serve(
            store = store,
            request = { state -> with(core) { state.awaitedSave() } },
        ) { request ->
            repository.saveLastAmount(request.amount)
            store.update { state -> with(core) { state.completeSave(request) } }
        }
    }

    fun acknowledgeMessage(message: SavedMessage) {
        store.update { state -> with(core) { state.acknowledgeMessage(message) } }
    }

    fun applyPreset(preset: RecipePreset) {
        store.update { state -> with(core) { state.applyPreset(preset) } }
    }
}
