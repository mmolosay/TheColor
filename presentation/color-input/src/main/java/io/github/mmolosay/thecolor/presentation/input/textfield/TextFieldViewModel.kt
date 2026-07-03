package io.github.mmolosay.thecolor.presentation.input.textfield

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.user.preferences.DefaultUserPreferences
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferencesRepository
import io.github.mmolosay.thecolor.domain.utils.filterReady
import io.github.mmolosay.thecolor.domain.utils.getOrElse
import io.github.mmolosay.thecolor.main.di.qualifiers.CoroutineDispatcherDiQualifiers.DefaultDispatcher
import io.github.mmolosay.thecolor.presentation.common.viewmodel.RequiresWriteOrdering
import io.github.mmolosay.thecolor.presentation.common.viewmodel.SimpleViewModel
import io.github.mmolosay.thecolor.presentation.common.viewmodel.Store
import io.github.mmolosay.thecolor.presentation.input.model.WithSource
import io.github.mmolosay.thecolor.presentation.input.model.causedByUser
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData.Text
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldFacade.ClearTextFeature
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Handles presentation logic of a single text field inside a 'Color Input' feature.
 *
 * Unlike typical `ViewModel`s, it doesn't derive from Google's [ViewModel][androidx.lifecycle.ViewModel],
 * thus cannot be instantiated using [ViewModelProvider][androidx.lifecycle.ViewModelProvider].
 *
 * Instead, it can be created within "simple" `ViewModel` or Google's `ViewModel`.
 */
class TextFieldViewModel @AssistedInject constructor(
    @Assisted coroutineScope: CoroutineScope,
    @Assisted private val store: Store<TextFieldData>,
    @Assisted val inputProcessor: TextFieldInputProcessor,
    private val userPreferencesRepository: UserPreferencesRepository,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : SimpleViewModel(coroutineScope) {

    private val orderedUpdates = defaultDispatcher.limitedParallelism(1)

    init {
        collectSelectAllTextOnTextFieldFocusPreference()
    }

    private fun collectSelectAllTextOnTextFieldFocusPreference() {
        coroutineScope.launch(defaultDispatcher) {
            userPreferencesRepository.flowOfSelectAllTextOnTextFieldFocus
                .filterReady()
                .map { it.result.getOrElse { DefaultUserPreferences.SelectAllTextOnTextFieldFocus } }
                .collectLatest { preference ->
                    store.update {
                        it.copy(shouldSelectAllTextOnFocus = preference.enabled)
                    }
                }
        }
    }

    fun facade(data: TextFieldData): TextFieldFacade =
        TextFieldFacadeImpl(
            data = data,
            viewModel = this,
        )

    fun updateText(textWithSource: WithSource<Text>) {
        @OptIn(RequiresWriteOrdering::class)
        coroutineScope.launch(orderedUpdates) {
            setText(textWithSource)
        }
    }

    @RequiresWriteOrdering
    suspend fun setText(textWithSource: WithSource<Text>) =
        store.update {
            it.copy(text = textWithSource)
        }

    @AssistedFactory
    fun interface Factory {
        fun create(
            coroutineScope: CoroutineScope,
            store: Store<TextFieldData>,
            inputProcessor: TextFieldInputProcessor,
        ): TextFieldViewModel
    }
}

class TextFieldDataFactory @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    fun create(
        text: WithSource<Text> = Text("") causedByUser false,
        isClearTextFeatureEnabled: Boolean,
    ): TextFieldData =
        TextFieldData(
            text = text,
            shouldSelectAllTextOnFocus = userPreferencesRepository
                .flowOfSelectAllTextOnTextFieldFocus
                .value.getOrElse { DefaultUserPreferences.SelectAllTextOnTextFieldFocus }
                .enabled,
            isClearTextFeatureEnabled = isClearTextFeatureEnabled,
        )
}

private class TextFieldFacadeImpl(
    private val data: TextFieldData,
    private val viewModel: TextFieldViewModel,
) : TextFieldFacade {

    override val text = data.text
    override fun setText(text: Text) {
        viewModel.updateText(text causedByUser true)
    }

    override val inputProcessor = viewModel.inputProcessor
    override val shouldSelectAllTextOnFocus = data.shouldSelectAllTextOnFocus
    override val clearTextFeature = run {
        if (!data.isClearTextFeatureEnabled) return@run null
        object : ClearTextFeature {
            override fun invoke() {
                viewModel.updateText(Text("") causedByUser true)
            }
        }
    }

    override fun equals(other: Any?): Boolean =
        other is TextFieldFacadeImpl && this.data == other.data && this.viewModel === other.viewModel

    override fun hashCode(): Int {
        var result = data.hashCode()
        result = 31 * result + System.identityHashCode(viewModel)
        return result
    }
}