package io.github.mmolosay.thecolor.presentation.input.rgb

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.color.Color
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
import io.github.mmolosay.thecolor.utils.Lens
import io.github.mmolosay.thecolor.utils.batch
import io.github.mmolosay.thecolor.utils.focus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbStateLenses as Lenses

/**
 * Handles presentation logic of the 'RGB Color Input' feature.
 *
 * Unlike typical `ViewModel`s, it doesn't derive from Google's [ViewModel][androidx.lifecycle.ViewModel],
 * thus cannot be instantiated using [ViewModelProvider][androidx.lifecycle.ViewModelProvider].
 *
 * Instead, it can be created within "simple" `ViewModel` or Google's `ViewModel`.
 */
class ColorInputRgbViewModel @AssistedInject constructor(
    @Assisted coroutineScope: CoroutineScope,
    @Assisted private val atom: Atom<ColorInputRgbState>,
    @Assisted private val submitAction: ColorInputSubmitAction,
    private val colorInputValidator: ColorInputValidator,
    private val userPreferencesRepository: UserPreferencesRepository,
) : SimpleViewModel(coroutineScope) {

    val rTextFieldHandle = TextFieldHandle(
        inputProcessor = TextFieldInputProcessorImpl,
        execute = ::executeRTextFieldAction,
    )
    val gTextFieldHandle = TextFieldHandle(
        inputProcessor = TextFieldInputProcessorImpl,
        execute = ::executeGTextFieldAction,
    )
    val bTextFieldHandle = TextFieldHandle(
        inputProcessor = TextFieldInputProcessorImpl,
        execute = ::executeBTextFieldAction,
    )

    init {
        collectSelectAllTextOnTextFieldFocusPreference()
        collectSmartBackspacePreference()
    }

    private fun collectSelectAllTextOnTextFieldFocusPreference() {
        coroutineScope.launch {
            userPreferencesRepository.flowOfSelectAllTextOnTextFieldFocus
                .filterReady()
                .map { it.result.getOrElse { DefaultUserPreferences.SelectAllTextOnTextFieldFocus } }
                .collect { preference ->
                    atom.batch {
                        focus(Lenses.rTextField).update {
                            it.withSelectAllTextOnFocus(value = preference.enabled)
                        }
                        focus(Lenses.gTextField).update {
                            it.withSelectAllTextOnFocus(value = preference.enabled)
                        }
                        focus(Lenses.bTextField).update {
                            it.withSelectAllTextOnFocus(value = preference.enabled)
                        }
                    }
                }
        }
    }

    private fun collectSmartBackspacePreference() {
        coroutineScope.launch {
            userPreferencesRepository.flowOfSmartBackspace
                .filterReady()
                .map { it.getOrElse { DefaultUserPreferences.SmartBackspace } }
                .collectLatest { preference ->
                    atom.update {
                        it.copy(isSmartBackspaceEnabled = preference.enabled)
                    }
                }
        }
    }

    fun execute(action: ColorInputRgbAction): Job? =
        when (action) {
            is ColorInputRgbAction.SubmitInput -> {
                submitInput()
                null
            }
            is ColorInputRgbAction.AckInputSubmissionResult -> {
                clearInputSubmissionResult()
                null
            }
        }

    fun executeRTextFieldAction(action: TextFieldAction): Job? =
        executeTextFieldAction(action, Lenses.rTextField)

    fun executeGTextFieldAction(action: TextFieldAction): Job? =
        executeTextFieldAction(action, Lenses.gTextField)

    fun executeBTextFieldAction(action: TextFieldAction): Job? =
        executeTextFieldAction(action, Lenses.bTextField)

    private fun executeTextFieldAction(
        action: TextFieldAction,
        lens: Lens<ColorInputRgbState, TextFieldData>,
    ): Job? {
        atom.focus(lens).update {
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
        atom.update {
            it.copy(inputSubmissionResult = result)
        }
    }

    private fun clearInputSubmissionResult() {
        atom.update {
            it.copy(inputSubmissionResult = null)
        }
    }

    private object TextFieldInputProcessorImpl : TextFieldInputProcessor {
        override fun invoke(input: String): TextFieldData.Text =
            input
                .filter { it.isDigit() }
                .take(3) // rgb component can be up to 3 digits long
                .let { string ->
                    if (string.isEmpty()) return@let ""
                    val rgbComponentMinValue = Color.Rgb.ComponentRange.first
                    val rgbComponentMaxValue = Color.Rgb.ComponentRange.last
                    var int = string.toIntOrNull() ?: rgbComponentMinValue // remove leading zeros
                    // reduce int from right until it's in range
                    while (int > rgbComponentMaxValue) {
                        int /= 10
                    }
                    int.toString()
                }
                .let { TextFieldData.Text(it) }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            coroutineScope: CoroutineScope,
            atom: Atom<ColorInputRgbState>,
            submitAction: ColorInputSubmitAction,
        ): ColorInputRgbViewModel
    }
}