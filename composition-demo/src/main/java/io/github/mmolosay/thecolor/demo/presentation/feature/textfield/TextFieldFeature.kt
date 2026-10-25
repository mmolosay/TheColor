package io.github.mmolosay.thecolor.demo.presentation.feature.textfield

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.utils.mapState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/**
 * A field running in one place, and the only way its View changes the field's state.
 *
 * An action's change is in the store by the time the action returns, so [viewData]'s value has it
 * at once, and a View that collects [viewData] has it by the next frame.
 */
class TextFieldFeature @AssistedInject constructor(
    @Assisted scope: CoroutineScope, // no work runs in it, but its parents needn't know that
    @Assisted private val store: Store<TextFieldState, *>,
    @Assisted private val environment: TextFieldEnvironment,
) {

    val viewData: StateFlow<TextFieldViewData> =
        store.mapState { field -> context(environment) { field.toViewData() } }

    fun enterText(text: String, expected: String) {
        store.update { field ->
            context(environment) { field.enterText(text, expected) }
        }
    }

    fun clearText() {
        store.update { field ->
            context(environment) { field.clearText() }
        }
    }

    /**
     * Creates a field in the place its parent chooses, given only what depends on that place.
     *
     * The scope must be a child of the parent's, so that the field's work ends with its parent's.
     * The environment must be the one the parent calls the field's operations with, so that the
     * parent and the field's own transitions change the field the same way.
     */
    @AssistedFactory
    fun interface Factory {
        fun create(
            scope: CoroutineScope,
            store: Store<TextFieldState, *>,
            environment: TextFieldEnvironment,
        ): TextFieldFeature
    }
}

// The transitions behind the actions, outside the class so that they can't reach its scope or
// store: a transition can run more than once, so it must have no side effects

/**
 * Returns the change behind [TextFieldFeature.enterText].
 *
 * Applies only if the field still holds [expected], the text that the edit was made on. Otherwise,
 * returns this same instance, so that the store changes nothing.
 */
context(_: TextFieldEnvironment)
fun TextFieldState.enterText(text: String, expected: String): Change<TextFieldState, Nothing> {
    if (this.text != expected) {
        return Change(this)
    }
    val state = this.withText(text)
    return Change(state)
}

context(_: TextFieldEnvironment)
fun TextFieldState.clearText(): Change<TextFieldState, Nothing> {
    val state = this.withText("")
    return Change(state)
}
