package io.github.mmolosay.thecolor.presentation.input.textfield

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.user.preferences.DefaultUserPreferences
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferencesRepository
import io.github.mmolosay.thecolor.domain.utils.filterReady
import io.github.mmolosay.thecolor.domain.utils.getOrElse
import io.github.mmolosay.thecolor.main.di.qualifiers.CoroutineDispatcherDiQualifiers.DefaultDispatcher
import io.github.mmolosay.thecolor.presentation.common.viewmodel.SimpleViewModel
import io.github.mmolosay.thecolor.presentation.input.model.WithSource
import io.github.mmolosay.thecolor.presentation.input.model.causedByUser
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData.Text
import io.github.mmolosay.thecolor.utils.Ref
import io.github.mmolosay.thecolor.utils.UpdateScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
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
    @Assisted private val ref: Ref<TextFieldData>,
    @Assisted val inputProcessor: TextFieldInputProcessor,
    private val userPreferencesRepository: UserPreferencesRepository,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : SimpleViewModel(coroutineScope) {

    private val exclusiveLane = defaultDispatcher.limitedParallelism(1)

    init {
        collectSelectAllTextOnTextFieldFocusPreference()
    }

    private fun collectSelectAllTextOnTextFieldFocusPreference() {
        coroutineScope.launch(defaultDispatcher) {
            userPreferencesRepository.flowOfSelectAllTextOnTextFieldFocus
                .filterReady()
                .map { it.result.getOrElse { DefaultUserPreferences.SelectAllTextOnTextFieldFocus } }
                .collectLatest { preference ->
                    ref.update {
                        it.copy(shouldSelectAllTextOnFocus = preference.enabled)
                    }
                }
        }
    }

    fun execute(action: TextFieldAction): Job =
        coroutineScope.launch(exclusiveLane) {
            when (action) {
                is TextFieldAction.SetText -> {
                    ref.run {
                        setText(action.text causedByUser true)
                    }
                }
                is TextFieldAction.ClearTextFeature.Invoke -> {
                    if (ref.value.isClearTextFeatureEnabled.not()) return@launch
                    ref.run {
                        setText(Text("") causedByUser true)
                    }
                }
            }
        }

    context(updateScope: UpdateScope<TextFieldData>)
    fun setText(textWithSource: WithSource<Text>) =
        updateScope.update {
            it.copy(text = textWithSource)
        }

    @AssistedFactory
    fun interface Factory {
        fun create(
            coroutineScope: CoroutineScope,
            ref: Ref<TextFieldData>,
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