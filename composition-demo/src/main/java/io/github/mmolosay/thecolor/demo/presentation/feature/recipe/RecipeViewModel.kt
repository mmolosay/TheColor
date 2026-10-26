package io.github.mmolosay.thecolor.demo.presentation.feature.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.composition.CompositionUtils.launchAwaited
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
    repository: RecipeRepository, // from dependency injection: only the work uses it
) : ViewModel() {

    // the screen's, which creates its children's, and so on down the tree
    private val core: RecipeCore = coreFactory.create()

    private val store = Store(initialState = core.initialState())

    val viewData: StateFlow<RecipeViewData> =
        store.mapState { state -> with(core) { state.toViewData() } }

    // for its View, which shows the children's Views
    val editor: AmountEditorFeature = editorFactory.create(
        scope = viewModelScope,
        store = store.slice(StateLenses.editor) { state, outcome: AmountEditorOutcome? ->
            with(core) { state.onEditor(outcome) }
        },
        core = core.editorCore,
    )

    val nutrition: NutritionFeature = nutritionFactory.create(
        scope = viewModelScope,
        store = store.slice(StateLenses.nutrition) { state, outcome: NutritionOutcome? ->
            with(core) { state.onNutrition(outcome) }
        },
        core = core.nutritionCore,
    )

    val substitutes: SubstitutesFeature = substitutesFactory.create(
        scope = viewModelScope,
        store = store.slice(StateLenses.substitutes) { state, outcome: SubstitutesOutcome? ->
            with(core) { state.onSubstitutes(outcome) }
        },
        core = core.substitutesCore,
    )

    // nothing to decide: the sheet has no outcomes, and the screen opens it in onSubstitutes
    val sheet: SubstituteSheetFeature = sheetFactory.create(
        scope = viewModelScope,
        store = store.slice(StateLenses.sheet) { state -> Change(state) },
        core = core.sheetCore,
    )

    // Nothing in the screen's functions reads or writes the servings, so the field gets a store of its
    // own, and its commits wake only its own View
    val servings: TextFieldFeature = textFieldFactory.create(
        scope = viewModelScope,
        store = Store(initialState = core.servingsCore.initialState(text = "1")),
        core = core.servingsCore,
    )

    init {
        // each kind of work on its own, so that one never restarts the other
        viewModelScope.launchAwaited(
            store = store,
            awaited = { state -> with(core) { state.awaitedLoad() } },
            block = { request ->
                val amount = repository.lastAmount()
                store.update { state -> with(core) { state.receiveLoad(request, amount) } }
            },
        )
        viewModelScope.launchAwaited(
            store = store,
            awaited = { state -> with(core) { state.awaitedSave() } },
            block = { request ->
                repository.saveLastAmount(request.amount)
                store.update { state -> with(core) { state.receiveSave(request) } }
            },
        )
    }

    fun messageShown(message: SavedMessage) {
        store.update { state -> with(core) { state.messageShown(message) } }
    }

    fun applyPreset(preset: RecipePreset) {
        store.update { state -> with(core) { state.applyPreset(preset) } }
    }
}
