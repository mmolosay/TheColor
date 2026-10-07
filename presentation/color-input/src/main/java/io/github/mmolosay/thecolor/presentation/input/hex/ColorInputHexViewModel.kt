package io.github.mmolosay.thecolor.presentation.input.hex

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.user.preferences.DefaultUserPreferences
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferencesRepository
import io.github.mmolosay.thecolor.domain.utils.filterReady
import io.github.mmolosay.thecolor.domain.utils.getOrElse
import io.github.mmolosay.thecolor.presentation.common.viewmodel.SimpleViewModel
import io.github.mmolosay.thecolor.presentation.input.ColorInputValidator
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmissionResult
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmitAction
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldAction
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldHandle
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldInputProcessor
import io.github.mmolosay.thecolor.presentation.input.textfield.reduce
import io.github.mmolosay.thecolor.presentation.input.textfield.withSelectAllTextOnFocus
import io.github.mmolosay.thecolor.utils.Atom
import io.github.mmolosay.thecolor.utils.focus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexStateLenses as Lenses

/**
 * Handles presentation logic of the 'HEX Color Input' feature.
 *
 * Unlike typical `ViewModel`s, it doesn't derive from Google's [ViewModel][androidx.lifecycle.ViewModel],
 * thus cannot be instantiated using [ViewModelProvider][androidx.lifecycle.ViewModelProvider].
 *
 * Instead, it can be created within "simple" `ViewModel` or Google's `ViewModel`.
 */
class ColorInputHexViewModel @AssistedInject constructor(
    @Assisted coroutineScope: CoroutineScope,
    @Assisted private val atom: Atom<ColorInputHexState>,
    @Assisted private val submitAction: ColorInputSubmitAction,
    private val colorInputValidator: ColorInputValidator,
    private val userPreferencesRepository: UserPreferencesRepository,
) : SimpleViewModel(coroutineScope) {

    val textFieldHandle = TextFieldHandle(
        inputProcessor = TextFieldInputProcessorImpl,
        execute = ::executeTextFieldAction,
    )

    init {
        collectSelectAllTextOnTextFieldFocusPreference()
    }

    private fun collectSelectAllTextOnTextFieldFocusPreference() {
        coroutineScope.launch {
            userPreferencesRepository.flowOfSelectAllTextOnTextFieldFocus
                .filterReady()
                .map { it.result.getOrElse { DefaultUserPreferences.SelectAllTextOnTextFieldFocus } }
                .collect { preference ->
                    atom.focus(Lenses.textField).update {
                        it.withSelectAllTextOnFocus(value = preference.enabled)
                    }
                }
        }
    }

    fun execute(action: ColorInputHexAction): Job? =
        when (action) {
            is ColorInputHexAction.SubmitInput -> {
                submitInput()
                null
            }
            is ColorInputHexAction.AckInputSubmissionResult -> {
                clearInputSubmissionResult()
                null
            }
        }

    fun executeTextFieldAction(action: TextFieldAction): Job? {
        atom.focus(Lenses.textField).update {
            it.reduce(action)
        }
        return null
    }

    private fun submitInput() {
        val colorInput = atom.value.colorInput()
        val validationResult = with(colorInputValidator) { colorInput.validate() }
        // outside 'atom.update': the callback may change the state itself
        val wasAccepted = submitAction.invoke(
            colorInput = colorInput,
            validationResult = validationResult,
        )
        val result = ColorInputSubmissionResult(wasAccepted)
        atom.update { it.copy(inputSubmissionResult = result) }
    }

    private fun clearInputSubmissionResult() {
        atom.update {
            it.copy(inputSubmissionResult = null)
        }
    }

    private object TextFieldInputProcessorImpl : TextFieldInputProcessor {
        override fun invoke(input: String): TextFieldData.Text =
            input
                .uppercase()
                .filter { it.isDigit() || it in 'A'..'F' }
                .take(6) // hex color can be up to 6 symbols long
                .let { TextFieldData.Text(it) }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            coroutineScope: CoroutineScope,
            atom: Atom<ColorInputHexState>,
            submitAction: ColorInputSubmitAction,
        ): ColorInputHexViewModel
    }
}