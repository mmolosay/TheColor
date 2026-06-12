package io.github.mmolosay.thecolor.presentation.input.hex

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.color.ColorConverter
import io.github.mmolosay.thecolor.main.di.qualifiers.CoroutineDispatcherDiQualifiers.DefaultDispatcher
import io.github.mmolosay.thecolor.main.di.qualifiers.CoroutineDispatcherDiQualifiers.UiDataUpdateDispatcher
import io.github.mmolosay.thecolor.presentation.common.viewmodel.SimpleViewModel
import io.github.mmolosay.thecolor.presentation.common.viewmodel.ViewModelCoroutineScope
import io.github.mmolosay.thecolor.presentation.input.ColorInputMapper
import io.github.mmolosay.thecolor.presentation.input.ColorInputMediator
import io.github.mmolosay.thecolor.presentation.input.ColorInputValidator
import io.github.mmolosay.thecolor.presentation.input.model.ColorInput
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmissionResult
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmitAction
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputValidationResult
import io.github.mmolosay.thecolor.presentation.input.model.getColorOrNull
import io.github.mmolosay.thecolor.presentation.input.plus
import io.github.mmolosay.thecolor.presentation.input.set
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldViewModel
import io.github.mmolosay.thecolor.presentation.input.textfield.data
import io.github.mmolosay.thecolor.presentation.input.textfield.updateText
import io.github.mmolosay.thecolor.utils.AckValue
import io.github.mmolosay.thecolor.utils.ActionWithResult
import io.github.mmolosay.thecolor.utils.asDelegate
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import io.github.mmolosay.thecolor.domain.color.ColorInputType as DomainColorInputType

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
    textFieldViewModelFactory: TextFieldViewModel.Factory,
    private val colorInputValidator: ColorInputValidator,
    private val colorInputMapper: ColorInputMapper,
    private val colorConverter: ColorConverter,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    @UiDataUpdateDispatcher private val uiDataUpdateDispatcher: CoroutineDispatcher,
) : SimpleViewModel(coroutineScope) {

    private val textFieldVm = textFieldViewModelFactory.create(
        // TODO: pass current color from the mediator as "initialText"?
        coroutineScope = ViewModelCoroutineScope(parent = coroutineScope),
        filterUserInput = ::filterUserInput,
        enableClearTextFeature = true,
    )

    private val stateFlow: MutableStateFlow<State> = run {
        val value = State(
            textFieldDerived = TextFieldDerived(textField = textFieldVm.data),
            submitInputResult = null,
        )
        MutableStateFlow(value)
    }
    private val state by stateFlow.asDelegate()

    val dataFlow: StateFlow<ColorInputHexData> = stateFlow
        .map { state -> ColorInputHexData(state) }
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.Eagerly + SharingStarted.WhileSubscribed(5000), // start eagerly to pre-compute first value before UI starts collecting
            initialValue = ColorInputHexData(state),
        )

    init {
        collectTextFieldData()
        collectMediatorUpdates()
    }

    private fun collectMediatorUpdates() {
        coroutineScope.launch(uiDataUpdateDispatcher) {
            mediator.colorStateFlow.collect { (color, source) ->
                // don't update text fields to avoid update loop if the color was set from this 'Color Input' type
                if (source == DomainColorInputType.Hex) return@collect
                val colorInput = if (color != null) {
                    val hexColor = with(colorConverter) { color.toHex() }
                    with(colorInputMapper) { hexColor.toColorInput() }
                } else {
                    EmptyColorInput
                }
                textFieldVm updateText TextFieldData.Text(colorInput.string)
            }
        }
    }

    private fun collectTextFieldData() {
        coroutineScope.launch(defaultDispatcher) {
            textFieldVm.dataFlow
                .onEach { textField ->
                    stateFlow.update {
                        it.copy(textFieldDerived = TextFieldDerived(textField))
                    }
                }
                .onEach { textField ->
                    // don't synchronize this data with other Views to avoid update loop
                    if (!textField.text.causedByUser) return@onEach
                    check(state.textFieldDerived.textField == textField)
                    val parsedColor = state.textFieldDerived.validationResult.getColorOrNull()
                    mediator.set(color = parsedColor, source = DomainColorInputType.Hex)
                }
                .collect()
        }
    }

    private fun filterUserInput(input: String): TextFieldData.Text =
        input
            .uppercase()
            .filter { it.isDigit() || it in 'A'..'F' }
            .take(6) // hex color can be up to 6 symbols long
            .let { TextFieldData.Text(it) }

    private fun submitInput() {
        val wasAccepted = submitAction.invoke(
            colorInput = state.textFieldDerived.colorInput,
            validationResult = state.textFieldDerived.validationResult,
        )
        val result = ColorInputSubmissionResult(wasAccepted)
        stateFlow.update { it.copy(submitInputResult = result) }
    }

    override fun dispose() {
        super.dispose()
        textFieldVm.dispose()
    }

    /**
     * A snapshot of all mutable values that are exposed via [ColorInputHexData].
     */
    private data class State(
        val textFieldDerived: TextFieldDerived,
        val submitInputResult: ColorInputSubmissionResult?,
    )

    private fun ColorInputHexData(
        state: State,
    ): ColorInputHexData {
        return ColorInputHexData(
            textField = state.textFieldDerived.textField,
            submitInput = ActionWithResult(
                result = state.submitInputResult?.let { result ->
                    AckValue(
                        value = result,
                        ack = { stateFlow.update { it.copy(submitInputResult = null) } },
                    )
                },
                action = ::submitInput,
            ),
        )
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