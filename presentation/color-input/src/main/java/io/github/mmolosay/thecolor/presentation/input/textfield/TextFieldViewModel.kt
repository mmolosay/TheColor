package io.github.mmolosay.thecolor.presentation.input.textfield

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.user.preferences.DefaultUserPreferences
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferencesRepository
import io.github.mmolosay.thecolor.domain.utils.filterReady
import io.github.mmolosay.thecolor.domain.utils.getOrElse
import io.github.mmolosay.thecolor.main.di.qualifiers.CoroutineDispatcherDiQualifiers.DefaultDispatcher
import io.github.mmolosay.thecolor.main.di.qualifiers.CoroutineDispatcherDiQualifiers.UiDataUpdateDispatcher
import io.github.mmolosay.thecolor.presentation.common.viewmodel.CompositionNode
import io.github.mmolosay.thecolor.presentation.common.viewmodel.CompositionScope
import io.github.mmolosay.thecolor.presentation.common.viewmodel.SimpleViewModel
import io.github.mmolosay.thecolor.presentation.input.model.WithSource
import io.github.mmolosay.thecolor.presentation.input.model.causedByUser
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData.ClearTextFeature
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData.Text
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Handles presentation logic of a single text field inside a 'Color Input' feature.
 *
 * Unlike typical `ViewModel`s, it doesn't derive from Google's [ViewModel][androidx.lifecycle.ViewModel],
 * thus cannot be instantiated using [ViewModelProvider][androidx.lifecycle.ViewModelProvider].
 *
 * Instead, it can be created within "simple" `ViewModel` or Google's `ViewModel`.
 */
class TextFieldViewModel @AssistedInject constructor(
    @Assisted initialText: String,
    @Assisted coroutineScope: CoroutineScope,
    @Assisted compositionScope: CompositionScope,
    @Assisted private val filterUserInput: (String) -> Text,
    @Assisted private val enableClearTextFeature: Boolean,
    private val userPreferencesRepository: UserPreferencesRepository,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    @UiDataUpdateDispatcher private val uiDataUpdateDispatcher: CoroutineDispatcher,
) : SimpleViewModel(coroutineScope) {

    private val orderedUpdates = defaultDispatcher.limitedParallelism(1)
    private val clearTextAction: () -> Unit = { updateText(Text("") causedByUser true) }

    val compositionNode = compositionScope.node(
        initialValue = run {
            val text = Text(initialText)
            TextFieldData(
                text = text causedByUser false, // coerce initial data to be caused by not a user,
                onTextChange = { text -> updateText(text causedByUser false) }, // the client of this ViewModel is a View, all text changes come from View (user)
                filterUserInput = filterUserInput,
                clearText = clearTextFeatureOrNull(text = text),
                shouldSelectAllTextOnFocus = userPreferencesRepository
                    .flowOfSelectAllTextOnTextFieldFocus
                    .value.getOrElse { DefaultUserPreferences.SelectAllTextOnTextFieldFocus }
                    .enabled,
            )
        },
    )
    val compositionNodeId: CompositionNode.Id
        get() = compositionNode.id
    val dataFlow: StateFlow<TextFieldData>
        get() = compositionNode.dataFlow

    init {
        collectSelectAllTextOnTextFieldFocusPreference()
    }

    private fun collectSelectAllTextOnTextFieldFocusPreference() {
        coroutineScope.launch(defaultDispatcher) {
            userPreferencesRepository.flowOfSelectAllTextOnTextFieldFocus
                .filterReady()
                .map { it.result.getOrElse { DefaultUserPreferences.SelectAllTextOnTextFieldFocus } }
                .collectLatest { preference ->
                    compositionNode.update {
                        it.copy(shouldSelectAllTextOnFocus = preference.enabled)
                    }
                }
        }
    }

    fun updateText(textWithSource: WithSource<Text>): Job =
        // Launching on the confined dispatcher schedules and applies updates in call order,
        // thus making this method fair
        coroutineScope.launch(orderedUpdates) {
//                withContext(uiDataUpdateDispatcher) { // TODO: is still needed? Test rapid text field changes
            setText(textWithSource)
        }

    suspend fun setText(textWithSource: WithSource<Text>) =
        compositionNode.update {
            it.smartCopy(textWithSource)
        }

    private fun TextFieldData.smartCopy(text: WithSource<Text>) =
        this.copy(
            text = text,
            clearText = clearTextFeatureOrNull(text = text.data),
        )

    private fun clearTextFeatureOrNull(text: Text): ClearTextFeature? {
        if (!enableClearTextFeature) return null
        return ClearTextFeatureImpl(
            willBeIdempotent = text.string.isEmpty(),
            invoke = clearTextAction,
        )
    }

    /** An implementation of the [ClearTextFeature] that supports meaningful equality check. */
    private data class ClearTextFeatureImpl(
        override val willBeIdempotent: Boolean,
        private val invoke: () -> Unit,
    ) : ClearTextFeature {
        override fun invoke() = this.invoke.invoke()
    }

    @AssistedFactory
    interface Factory {

        /**
         * @param initialText declared type is a [String], but it is intended to be a [TextFieldData.Text].
         * At the moment, Dagger doesn't deal with name mangling correctly, which occurs due to
         * the [TextFieldData.Text] being a `value class`.
         * https://github.com/google/dagger/issues/4613
         * https://kotlinlang.org/docs/inline-classes.html#mangling
         * Taking this into account, the `ViewModel` will treat the [initialText] as [TextFieldData.Text] and
         * won't process it in any way, as it would've done for a raw user input [String].
         * See [TextFieldData.filterUserInput].
         */
        fun create(
            initialText: String = "",
            coroutineScope: CoroutineScope,
            compositionScope: CompositionScope,
            filterUserInput: (String) -> Text,
            enableClearTextFeature: Boolean,
        ): TextFieldViewModel
    }
}

val TextFieldViewModel.data: TextFieldData
    get() = this.dataFlow.value