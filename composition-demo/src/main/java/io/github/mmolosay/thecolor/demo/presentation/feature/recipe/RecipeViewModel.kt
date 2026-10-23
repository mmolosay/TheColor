package io.github.mmolosay.thecolor.demo.presentation.feature.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.composition.CompositionUtils.launchAwaited
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.composition.Token
import io.github.mmolosay.thecolor.composition.ViewDataSource
import io.github.mmolosay.thecolor.composition.viewData
import io.github.mmolosay.thecolor.demo.domain.Amount
import io.github.mmolosay.thecolor.demo.domain.RecipeRepository
import io.github.mmolosay.thecolor.demo.presentation.feature.editor.AmountEditorFeature
import io.github.mmolosay.thecolor.demo.presentation.feature.editor.AmountEditorOutcome
import io.github.mmolosay.thecolor.demo.presentation.feature.editor.AmountEditorState
import io.github.mmolosay.thecolor.demo.presentation.feature.editor.isEmpty
import io.github.mmolosay.thecolor.demo.presentation.feature.editor.withAmount
import io.github.mmolosay.thecolor.demo.presentation.feature.editor.withSubmissionAccepted
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.NutritionFeature
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.NutritionState
import io.github.mmolosay.thecolor.utils.Lens
import javax.inject.Inject

@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val environment: RecipeEnvironment,
    editorFactory: AmountEditorFeature.Factory,
    nutritionFactory: NutritionFeature.Factory,
    private val repository: RecipeRepository, // from dependency injection: only the work uses it
) : ViewModel() {

    private val store = run {
        val state = context(environment) { RecipeStateFactory.create() }
        Store(initialState = state)
    }

    val viewData: ViewDataSource<RecipeViewData> =
        store.viewData { recipe -> context(environment) { recipe.toViewData() } }

    // for its View, which shows the editor's View
    val editor: AmountEditorFeature = run {
        val slice = store.slice<AmountEditorState, AmountEditorOutcome>(editorLens) { recipe, outcome ->
            context(environment) { onEditor(recipe, outcome) }
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

    init {
        // each kind of work on its own, so that one never restarts the other
        viewModelScope.launchAwaited(
            store = store,
            awaited = { recipe -> recipe.load },
            block = { request ->
                val amount = repository.lastAmount()
                store.update { recipe ->
                    context(environment) { recipe.receiveLoad(request, amount) }
                }
            },
        )
        viewModelScope.launchAwaited(
            store = store,
            awaited = { recipe -> context(environment) { recipe.awaitedSave() } },
            block = { request ->
                repository.saveLastAmount(request.amount)
                store.update { recipe ->
                    context(environment) { recipe.receiveSave(request) }
                }
            },
        )
    }

    fun messageShown(token: Token) {
        store.update { recipe ->
            context(environment) { recipe.messageShown(token) }
        }
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

// The reducer for the editor. Only the screen's ViewModel runs it, inside the editor's update, for
// any change of the editor's own, the user's or not. The nutrition card follows the amount the change
// leaves. A submitted amount is saved, and its submission waits until it is
context(_: RecipeEnvironment)
fun onEditor(
    recipe: RecipeState,
    outcome: AmountEditorOutcome?,
): Change<RecipeState, Nothing> {
    val state = recipe.withNutritionForAmount()
    return when (outcome) {
        null -> Change(state)
        is AmountEditorOutcome.Submitted -> {
            val save = SaveRequest(amount = outcome.amount)
            Change(state.copy(save = save))
        }
    }
}

// The transitions behind the actions and the results. Only the screen's ViewModel calls them.
// They're outside the class so that they can't reach its lifetime or store: they can run more than
// once, so they must have no side effects

// The saved amount fills only the empty editor the screen opened with, so that it never replaces
// what the user typed meanwhile. The editor is the expected value: what the editor holds tells, not
// which changes it went through, since some of them aren't the user's, such as a setting arriving
context(environment: RecipeEnvironment)
fun RecipeState.receiveLoad(
    request: LoadRequest,
    amount: Amount?,
): Change<RecipeState, Nothing> {
    if (this.load !== request) return Change(this) // already received
    val isEmpty = context(environment.editor) { this.editor.isEmpty() }
    if (!isEmpty) return Change(this.copy(load = null)) // the user has typed meanwhile
    val editor = context(environment.editor) { this.editor.withAmount(amount) }
    val state = this.copy(editor = editor, load = null).withNutritionForAmount()
    return Change(state)
}

// Applies only to the save the screen still waits for. Accepts the submission it was made for, and
// asks for the message, in one change, so that neither happens without the other
context(environment: RecipeEnvironment)
fun RecipeState.receiveSave(request: SaveRequest): Change<RecipeState, Nothing> {
    if (this.awaitedSave() !== request) return Change(this) // withdrawn, or a newer one is saved
    val editor = context(environment.editor) { this.editor.withSubmissionAccepted() }
    val message = SavedMessage(token = Token(), amount = request.amount)
    val state = this.copy(editor = editor, save = null, messages = this.messages + message)
    return Change(state)
}

// what's left once the View has shown the message for this token: a newer message stays
context(_: RecipeEnvironment)
fun RecipeState.messageShown(token: Token): Change<RecipeState, Nothing> {
    val messages = this.messages.filterNot { message -> message.token === token }
    val state = this.copy(messages = messages)
    return Change(state)
}
