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
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmitAction
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputValidationResult
import io.github.mmolosay.thecolor.presentation.input.model.ColorSubmissionResult
import io.github.mmolosay.thecolor.presentation.input.model.DataState
import io.github.mmolosay.thecolor.presentation.input.model.getColorOrNull
import io.github.mmolosay.thecolor.presentation.input.plus
import io.github.mmolosay.thecolor.presentation.input.set
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldViewModel
import io.github.mmolosay.thecolor.presentation.input.textfield.updateText
import io.github.mmolosay.thecolor.utils.MutableConsumableStore
import io.github.mmolosay.thecolor.utils.asConsumableStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
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

    val dataStateFlow: StateFlow<DataState<ColorInputHexData>> =
        textFieldVm.dataFlow
            .map { textField ->
                val colorInput = ColorInput.Hex(string = textField.text.data.string)
                val validationResult = with(colorInputValidator) { colorInput.validate() }
                FullData(
                    textField = textField,
                    submitInput = { submitInput(colorInput, validationResult) },
                    colorInput = colorInput,
                    colorInputValidationResult = validationResult,
                )
            }
            .onEach { fullData ->
                // don't synchronize this data with other Views to avoid update loop
                if (!fullData.textField.text.causedByUser) return@onEach
                val parsedColor = fullData.colorInputValidationResult.getColorOrNull()
                mediator.set(color = parsedColor, source = DomainColorInputType.Hex)
            }
            .map { fullData -> fullData.reduce() }
            .map { data -> DataState(data) }
            .flowOn(defaultDispatcher)
            .stateIn(
                scope = coroutineScope,
                started = SharingStarted.Eagerly + SharingStarted.WhileSubscribed(5000), // start eagerly to pre-compute first value before UI starts collecting
                initialValue = DataState.BeingInitialized,
            )

    private val _submissionResultStore = MutableConsumableStore<ColorSubmissionResult>()
    val submissionResultStore = _submissionResultStore.asConsumableStore()

    init {
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

    private fun filterUserInput(input: String): TextFieldData.Text =
        input
            .uppercase()
            .filter { it.isDigit() || it in 'A'..'F' }
            .take(6) // hex color can be up to 6 symbols long
            .let { TextFieldData.Text(it) }

    private fun submitInput(
        colorInput: ColorInput.Hex,
        validationResult: ColorInputValidationResult,
    ) {
        val wasAccepted = submitAction.invoke(
            colorInput = colorInput,
            validationResult = validationResult,
        )
        val result = ColorSubmissionResult(wasAccepted)
        _submissionResultStore.publish(result)
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
 * @param colorInputValidationResult is a result of [colorInput] validation.
 */
private data class FullData(
    val textField: TextFieldData,
    val submitInput: () -> Unit,
    val colorInput: ColorInput.Hex,
    val colorInputValidationResult: ColorInputValidationResult,
)

/**
 * Reduces [FullData] to the [ColorInputHexData] which is exposed from the ViewModel.
 */
private fun FullData.reduce(): ColorInputHexData =
    ColorInputHexData(
        textField = textField,
        submitInput = submitInput,
    )