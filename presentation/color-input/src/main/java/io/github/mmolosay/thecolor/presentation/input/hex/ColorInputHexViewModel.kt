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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
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

    private val textFieldDerivedFlow: MutableStateFlow<TextFieldDerived> = run {
        val value = TextFieldDerived(textField = textFieldVm.data)
        MutableStateFlow(value)
    }
    private val textFieldDerived by textFieldDerivedFlow.asDelegate()

    private val submitInputResultFlow: MutableStateFlow<ColorInputSubmissionResult?> =
        MutableStateFlow(null)
    private val submitInputResult by submitInputResultFlow.asDelegate()

    val dataFlow: StateFlow<ColorInputHexData> =
        combine(
            textFieldDerivedFlow,
            submitInputResultFlow,
        ) { textFieldDerived, submitInputResult ->
            ColorInputHexData(textFieldDerived, submitInputResult)
        }
            .stateIn(
                scope = coroutineScope,
                started = SharingStarted.Eagerly + SharingStarted.WhileSubscribed(5000), // start eagerly to pre-compute first value before UI starts collecting
                initialValue = ColorInputHexData(textFieldDerived, submitInputResult),
            )

    private fun ColorInputHexData(
        textFieldDerived: TextFieldDerived,
        submitInputResult: ColorInputSubmissionResult?,
    ): ColorInputHexData {
        return ColorInputHexData(
            textField = textFieldDerived.textField,
            submitInput = ActionWithResult(
                result = submitInputResult?.let { result ->
                    AckValue(
                        value = result,
                        ack = { submitInputResultFlow.value = null },
                    )
                },
                action = ::submitInput,
            ),
        )
    }

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
                    textFieldDerivedFlow.value = TextFieldDerived(textField)
                }
                .onEach { textField ->
                    // don't synchronize this data with other Views to avoid update loop
                    if (!textField.text.causedByUser) return@onEach
                    val parsedColor = textFieldDerived.validationResult.getColorOrNull()
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
            colorInput = textFieldDerived.colorInput,
            validationResult = textFieldDerived.validationResult,
        )
        submitInputResultFlow.value = ColorInputSubmissionResult(wasAccepted)
    }

    override fun dispose() {
        super.dispose()
        textFieldVm.dispose()
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