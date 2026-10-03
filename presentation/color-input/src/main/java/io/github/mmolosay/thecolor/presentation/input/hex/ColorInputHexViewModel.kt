package io.github.mmolosay.thecolor.presentation.input.hex

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorConverter
import io.github.mmolosay.thecolor.domain.user.preferences.DefaultUserPreferences
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferencesRepository
import io.github.mmolosay.thecolor.domain.utils.filterReady
import io.github.mmolosay.thecolor.domain.utils.getOrElse
import io.github.mmolosay.thecolor.main.di.qualifiers.CoroutineDispatcherDiQualifiers.DefaultDispatcher
import io.github.mmolosay.thecolor.presentation.common.viewmodel.SimpleViewModel
import io.github.mmolosay.thecolor.presentation.input.ColorInputMapper
import io.github.mmolosay.thecolor.presentation.input.ColorInputMediator
import io.github.mmolosay.thecolor.presentation.input.ColorInputSource
import io.github.mmolosay.thecolor.presentation.input.ColorInputValidator
import io.github.mmolosay.thecolor.presentation.input.model.ColorInput
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmissionResult
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmitAction
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputValidationResult
import io.github.mmolosay.thecolor.presentation.input.model.causedByUser
import io.github.mmolosay.thecolor.presentation.input.model.getColorOrNull
import io.github.mmolosay.thecolor.presentation.input.set
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldAction
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldDataFactory
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldHandle
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldInputProcessor
import io.github.mmolosay.thecolor.presentation.input.textfield.reduce
import io.github.mmolosay.thecolor.presentation.input.textfield.withSelectAllTextOnFocus
import io.github.mmolosay.thecolor.presentation.input.textfield.withText
import io.github.mmolosay.thecolor.utils.asUpdateScope
import io.github.mmolosay.thecolor.utils.focus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import io.github.mmolosay.thecolor.domain.color.ColorInputType as DomainColorInputType
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexDataLenses as Lenses

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
    @Assisted private val mediator: ColorInputMediator,
    @Assisted private val submitAction: ColorInputSubmitAction,
    dataFactory: ColorInputHexDataFactory,
    private val colorInputValidator: ColorInputValidator,
    private val colorInputMapper: ColorInputMapper,
    private val colorConverter: ColorConverter,
    private val userPreferencesRepository: UserPreferencesRepository,
    @DefaultDispatcher defaultDispatcher: CoroutineDispatcher,
) : SimpleViewModel(coroutineScope) {

    private val exclusiveLane = defaultDispatcher.limitedParallelism(1)

    private val _dataFlow = MutableStateFlow(dataFactory.create())
    val dataFlow: StateFlow<ColorInputHexData> = _dataFlow.asStateFlow()

    private val textFieldInputProcessor = TextFieldInputProcessorImpl()
    val textFieldHandle = TextFieldHandle(
        inputProcessor = textFieldInputProcessor,
        execute = ::executeTextFieldAction,
    )

    init {
        collectSelectAllTextOnTextFieldFocusPreference()
        collectMediatorUpdates()
        collectTextFieldData()
    }

    private fun collectSelectAllTextOnTextFieldFocusPreference() {
        coroutineScope.launch {
            userPreferencesRepository.flowOfSelectAllTextOnTextFieldFocus
                .filterReady()
                .map { it.result.getOrElse { DefaultUserPreferences.SelectAllTextOnTextFieldFocus } }
                .collect { preference ->
                    _dataFlow.asUpdateScope().focus(Lenses.textField).update {
                        it.withSelectAllTextOnFocus(value = preference.enabled)
                    }
                }
        }
    }

    private fun collectMediatorUpdates() {
        coroutineScope.launch {
            mediator.colorStateFlow.collect { (color, source) ->
                // don't update text fields to avoid update loop if the color was set from this 'Color Input' type
                if (source is ColorInputSource && source.type == DomainColorInputType.Hex) return@collect
                val colorInput = if (color != null) {
                    val hexColor = with(colorConverter) { color.toHex() }
                    with(colorInputMapper) { hexColor.toColorInput() }
                } else {
                    EmptyColorInput
                }
                _dataFlow.asUpdateScope().focus(Lenses.textField).update {
                    val textWithSource = TextFieldData.Text(colorInput.string) causedByUser false
                    it.withText(textWithSource)
                }
            }
        }
    }

    private fun collectTextFieldData() {
        coroutineScope.launch {
            _dataFlow
                .map { data -> TextFieldDerived(data.textField) }
                .distinctUntilChangedBy { derived -> derived.color } // only update mediator when color changes
                .collectLatest collect@{ derived ->
                    // don't synchronize this data with other Views to avoid update loop
                    if (!derived.textField.text.causedByUser) return@collect
                    mediator.set(
                        color = derived.color,
                        source = ColorInputSource(DomainColorInputType.Hex),
                    )
                }
        }
    }

    fun execute(action: ColorInputHexAction): Job =
        coroutineScope.launch(exclusiveLane) {
            when (action) {
                is ColorInputHexAction.SubmitInput -> {
                    submitInput()
                }
                is ColorInputHexAction.AckInputSubmissionResult -> {
                    clearInputSubmissionResult()
                }
            }
        }

    fun executeTextFieldAction(action: TextFieldAction): Job =
        coroutineScope.launch(exclusiveLane) {
            _dataFlow.asUpdateScope().focus(Lenses.textField).update {
                it.reduce(action)
            }
        }

    private fun submitInput() {
        val textField = dataFlow.value.textField
        val derived = TextFieldDerived(textField)
        val wasAccepted = submitAction.invoke(
            colorInput = derived.colorInput,
            validationResult = derived.validationResult,
        )
        val result = ColorInputSubmissionResult(wasAccepted)
        _dataFlow.update {
            it.copy(inputSubmissionResult = result)
        }
    }

    private fun clearInputSubmissionResult() {
        _dataFlow.update {
            it.copy(inputSubmissionResult = null)
        }
    }

    private fun TextFieldDerived(
        textField: TextFieldData,
    ): TextFieldDerived {
        val colorInput = ColorInput.Hex(string = textField.text.data.string)
        val validationResult = with(colorInputValidator) { colorInput.validate() }
        return TextFieldDerived(
            textField = textField,
            colorInput = colorInput,
            validationResult = validationResult,
        )
    }

    private class TextFieldInputProcessorImpl : TextFieldInputProcessor {
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
            mediator: ColorInputMediator,
            submitAction: ColorInputSubmitAction,
        ): ColorInputHexViewModel
    }

    companion object {
        val EmptyColorInput = ColorInput.Hex(string = "")
    }
}

/**
 * Couples [TextFieldData] with values that are derived from it.
 */
private data class TextFieldDerived(
    val textField: TextFieldData,
    val colorInput: ColorInput.Hex,
    val validationResult: ColorInputValidationResult,
)

private val TextFieldDerived.color: Color?
    get() = this.validationResult.getColorOrNull()

class ColorInputHexDataFactory @Inject constructor(
    private val textFieldDataFactory: TextFieldDataFactory,
) {
    fun create(): ColorInputHexData =
        ColorInputHexData(
            textField = textFieldDataFactory.create(
                text = TextFieldData.Text("") causedByUser false,
                isClearTextFeatureEnabled = true,
            ),
            inputSubmissionResult = null,
        )
}