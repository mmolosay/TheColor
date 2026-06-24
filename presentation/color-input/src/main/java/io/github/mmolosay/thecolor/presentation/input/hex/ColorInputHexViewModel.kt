package io.github.mmolosay.thecolor.presentation.input.hex

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.color.ColorConverter
import io.github.mmolosay.thecolor.main.di.qualifiers.CoroutineDispatcherDiQualifiers.DefaultDispatcher
import io.github.mmolosay.thecolor.presentation.common.viewmodel.CompositionNode
import io.github.mmolosay.thecolor.presentation.common.viewmodel.CompositionScope
import io.github.mmolosay.thecolor.presentation.common.viewmodel.SimpleViewModel
import io.github.mmolosay.thecolor.presentation.common.viewmodel.ViewModelCoroutineScope
import io.github.mmolosay.thecolor.presentation.input.ColorInputMapper
import io.github.mmolosay.thecolor.presentation.input.ColorInputMediator
import io.github.mmolosay.thecolor.presentation.input.ColorInputValidator
import io.github.mmolosay.thecolor.presentation.input.model.ColorInput
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmissionResult
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmitAction
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputValidationResult
import io.github.mmolosay.thecolor.presentation.input.model.causedByUser
import io.github.mmolosay.thecolor.presentation.input.model.getColorOrNull
import io.github.mmolosay.thecolor.presentation.input.set
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldViewModel
import io.github.mmolosay.thecolor.presentation.input.textfield.data
import io.github.mmolosay.thecolor.utils.ActionWithResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
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
    @Assisted compositionScope: CompositionScope,
    @Assisted private val mediator: ColorInputMediator,
    @Assisted private val submitAction: ColorInputSubmitAction,
    textFieldViewModelFactory: TextFieldViewModel.Factory,
    private val colorInputValidator: ColorInputValidator,
    private val colorInputMapper: ColorInputMapper,
    private val colorConverter: ColorConverter,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : SimpleViewModel(coroutineScope) {

    private val textFieldVm = textFieldViewModelFactory.create(
        // TODO: pass current color from the mediator as "initialText"?
        coroutineScope = ViewModelCoroutineScope(parent = coroutineScope),
        compositionScope = compositionScope,
        filterUserInput = ::filterUserInput,
        enableClearTextFeature = true,
    )

    private val compositionNode = compositionScope.node(
        initialValue = run {
            ColorInputHexData(
                textField = textFieldVm.data,
                submitInput = ActionWithResult(
                    result = null,
                    action = ::submitInput,
                ),
            )
        },
        recompute = { current ->
            val textField = textFieldVm.data
            current.copy(
                textField = textField,
            )
        },
        children = listOf(textFieldVm.compositionNodeId),
    )
    val compositionNodeId: CompositionNode.Id
        get() = compositionNode.id
    val dataFlow: StateFlow<ColorInputHexData>
        get() = compositionNode.dataFlow

    init {
        collectMediatorUpdates()
        collectTextFieldData()
    }

    private fun collectMediatorUpdates() {
        coroutineScope.launch(defaultDispatcher) {
            mediator.colorStateFlow.collect { (color, source) ->
                // don't update text fields to avoid update loop if the color was set from this 'Color Input' type
                if (source == DomainColorInputType.Hex) return@collect
                val colorInput = if (color != null) {
                    val hexColor = with(colorConverter) { color.toHex() }
                    with(colorInputMapper) { hexColor.toColorInput() }
                } else {
                    EmptyColorInput
                }
                run {
                    val textWithSource = TextFieldData.Text(colorInput.string) causedByUser false
                    textFieldVm.updateText(textWithSource)
                }
            }
        }
    }

    private fun collectTextFieldData() {
        coroutineScope.launch(defaultDispatcher) {
            textFieldVm.dataFlow.collectLatest collect@{ textField ->
                // don't synchronize this data with other Views to avoid update loop
                if (!textField.text.causedByUser) return@collect
                val parsedColor = TextFieldDerived(textField).validationResult.getColorOrNull()
                mediator.set(color = parsedColor, source = DomainColorInputType.Hex)
            }
        }
    }

    private fun filterUserInput(input: String): TextFieldData.Text =
        input
            .uppercase()
            .filter { it.isDigit() || it in 'A'..'F' }
            .take(6) // hex color can be up to 6 symbols long
            .let { TextFieldData.Text(it) }

    private fun submitInput() {
        val derived = TextFieldDerived(textFieldVm.data)
        val wasAccepted = submitAction.invoke(
            colorInput = derived.colorInput,
            validationResult = derived.validationResult,
        )
        val result = ColorInputSubmissionResult(wasAccepted)
        coroutineScope.launch {
            compositionNode.update {
                it.copy(
                    submitInput = ActionWithResult(
                        result = result,
                        resultAck = ::clearSubmitInputResult,
                        action = ::submitInput,
                    )
                )
            }
        }
    }

    private fun clearSubmitInputResult() {
        coroutineScope.launch {
            compositionNode.update {
                it.copy(
                    submitInput = it.submitInput.copy(result = null),
                )
            }
        }
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
            compositionScope: CompositionScope,
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