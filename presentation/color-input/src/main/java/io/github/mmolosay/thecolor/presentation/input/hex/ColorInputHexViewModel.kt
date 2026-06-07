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
import io.github.mmolosay.thecolor.presentation.input.hex.StatefulData.State
import io.github.mmolosay.thecolor.presentation.input.model.ColorInput
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmissionResult
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmitAction
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputValidationResult
import io.github.mmolosay.thecolor.presentation.input.model.DataState
import io.github.mmolosay.thecolor.presentation.input.model.getColorOrNull
import io.github.mmolosay.thecolor.presentation.input.plus
import io.github.mmolosay.thecolor.presentation.input.set
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldViewModel
import io.github.mmolosay.thecolor.presentation.input.textfield.updateText
import io.github.mmolosay.thecolor.utils.AckValue
import io.github.mmolosay.thecolor.utils.ActionWithResult
import io.github.mmolosay.thecolor.utils.Maybe
import io.github.mmolosay.thecolor.utils.asDelegate
import io.github.mmolosay.thecolor.utils.requireValue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
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

    private val statefulDataFlow: MutableStateFlow<StatefulData> = run {
        val value = StatefulData(
            textField = Maybe.None,
            colorInput = Maybe.None,
            validationResult = Maybe.None,
            submitInput = ::submitInput,
            submitInputResult = null,
            state = State.BeingInitialized,
        )
        MutableStateFlow(value)
    }
    private val statefulData: StatefulData by statefulDataFlow.asDelegate()

    val dataStateFlow: StateFlow<DataState<ColorInputHexData>> = statefulDataFlow
        .map { it.toDataState() }
        .flowOn(defaultDispatcher)
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.Eagerly + SharingStarted.WhileSubscribed(5000), // start eagerly to pre-compute first value before UI starts collecting
            initialValue = DataState.BeingInitialized,
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
                    val colorInput = ColorInput.Hex(string = textField.text.data.string)
                    val validationResult = with(colorInputValidator) { colorInput.validate() }
                    statefulDataFlow.update {
                        it.copy(
                            state = State.Ready,
                            textField = Maybe.Some(textField),
                            colorInput = Maybe.Some(colorInput),
                            validationResult = Maybe.Some(validationResult),
                        )
                    }
                }
                .onEach { textField ->
                    // don't synchronize this data with other Views to avoid update loop
                    if (!textField.text.causedByUser) return@onEach
                    val parsedColor = statefulData.validationResult.requireValue().getColorOrNull()
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

    private fun submitInput(
        colorInput: ColorInput.Hex = statefulData.colorInput.requireValue(),
        validationResult: ColorInputValidationResult = statefulData.validationResult.requireValue(),
    ) {
        val wasAccepted = submitAction.invoke(
            colorInput = colorInput,
            validationResult = validationResult,
        )
        val ackResult = AckValue(
            value = ColorInputSubmissionResult(wasAccepted),
            ack = {
                statefulDataFlow.update {
                    it.copy(submitInputResult = null)
                }
            },
        )
        statefulDataFlow.update {
            it.copy(submitInputResult = ackResult)
        }
    }

    override fun dispose() {
        super.dispose()
        textFieldVm.dispose()
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
 * Couples data which is exposed from the ViewModel with various values that are related to it:
 * derived from the exposed data, or used to produce it.
 *
 * @param colorInput contains data from the [textField].
 * @param validationResult is a result of [colorInput] validation.
 */
private data class StatefulData(
    val textField: Maybe<TextFieldData>,
    val colorInput: Maybe<ColorInput.Hex>,
    val validationResult: Maybe<ColorInputValidationResult>,
    val submitInput: () -> Unit,
    val submitInputResult: AckValue<ColorInputSubmissionResult>?,
    val state: State,
) {

    enum class State {
        BeingInitialized, Ready,
    }
}

private fun StatefulData.toDataState(): DataState<ColorInputHexData> =
    when (this.state) {
        State.BeingInitialized -> DataState.BeingInitialized
        State.Ready -> DataState.Ready(data = this.toData())
    }

private fun StatefulData.toData(): ColorInputHexData {
    return ColorInputHexData(
        textField = this.textField.requireValue(),
        submitInput = ActionWithResult(
            action = this.submitInput,
            result = this.submitInputResult,
        ),
    )
}