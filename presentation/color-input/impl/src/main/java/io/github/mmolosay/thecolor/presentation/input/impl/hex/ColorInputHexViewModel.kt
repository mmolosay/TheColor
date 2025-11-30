package io.github.mmolosay.thecolor.presentation.input.impl.hex

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.presentation.common.viewmodel.SimpleViewModel
import io.github.mmolosay.thecolor.presentation.common.viewmodel.ViewModelCoroutineScope
import io.github.mmolosay.thecolor.presentation.input.api.ColorInput
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputEventStore
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputSubmitAction
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputValidationResult
import io.github.mmolosay.thecolor.presentation.input.api.getColorOrNull
import io.github.mmolosay.thecolor.presentation.input.impl.ColorInputMediator
import io.github.mmolosay.thecolor.presentation.input.impl.ColorInputValidator
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldData
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldData.Text
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldViewModel
import io.github.mmolosay.thecolor.presentation.input.impl.field.updateText
import io.github.mmolosay.thecolor.presentation.input.impl.model.ColorSubmissionResult
import io.github.mmolosay.thecolor.presentation.input.impl.model.DataState
import io.github.mmolosay.thecolor.presentation.input.impl.model.WithSource
import io.github.mmolosay.thecolor.presentation.input.impl.plus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Named
import io.github.mmolosay.thecolor.domain.model.ColorInputType as DomainColorInputType

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
    @Assisted private val eventStore: ColorInputEventStore,
    @Assisted private val submitAction: ColorInputSubmitAction,
    textFieldViewModelFactory: TextFieldViewModel.Factory,
    private val colorInputValidator: ColorInputValidator,
    @Named("defaultDispatcher") private val defaultDispatcher: CoroutineDispatcher,
    @Named("uiDataUpdateDispatcher") private val uiDataUpdateDispatcher: CoroutineDispatcher,
) : SimpleViewModel(coroutineScope) {

    private val textFieldVm = textFieldViewModelFactory.create(
        coroutineScope = ViewModelCoroutineScope(parent = coroutineScope),
        filterUserInput = ::filterUserInput,
        enableClearTextFeature = true,
    )

    val dataStateFlow: StateFlow<DataState<ColorInputHexData>> =
        textFieldVm.dataFlow
            .map { textFieldWithSource ->
                val textField = textFieldWithSource.data
                val colorInput = ColorInput.Hex(string = textField.text.string)
                val validationResult = with(colorInputValidator) { colorInput.validate() }
                val fullData = FullData(
                    textField = textField,
                    submitInput = { submitInput(colorInput, validationResult) },
                    colorInput = colorInput,
                    colorInputValidationResult = validationResult,
                )
                WithSource(data = fullData, causedByUser = textFieldWithSource.causedByUser)
            }
            .onEach { fullDataWithSource ->
                // don't synchronize this data with other Views to avoid update loop
                if (!fullDataWithSource.causedByUser) return@onEach
                val parsedColor = fullDataWithSource.data.colorInputValidationResult.getColorOrNull()
                mediator.send(color = parsedColor, from = DomainColorInputType.Hex)
            }
            .map { fullDataWithSource -> fullDataWithSource.data }
            .map { fullData -> fullData.reduce() }
            .map { data -> DataState(data) }
            .flowOn(defaultDispatcher)
            .stateIn(
                scope = coroutineScope,
                started = SharingStarted.Eagerly + SharingStarted.WhileSubscribed(5000), // start eagerly to pre-compute first value before UI starts collecting
                initialValue = DataState.BeingInitialized,
            )

    private val _colorSubmissionResultFlow = MutableStateFlow<ColorSubmissionResult?>(null)
    val colorSubmissionResultFlow = _colorSubmissionResultFlow.asStateFlow()

    init {
        collectMediatorUpdates()
    }

    private fun collectMediatorUpdates() {
        coroutineScope.launch(uiDataUpdateDispatcher) {
            mediator.hexColorInputFlow.collect { input ->
                textFieldVm updateText Text(input.string)
            }
        }
    }

    private fun filterUserInput(input: String): Text =
        input
            .uppercase()
            .filter { it.isDigit() || it in 'A'..'F' }
            .take(6) // hex color can be up to 6 symbols long
            .let { Text(it) }

    private fun submitInput(
        colorInput: ColorInput.Hex,
        validationResult: ColorInputValidationResult,
    ) {
        val wasAccepted = submitAction.invoke(
            colorInput = colorInput,
            validationResult = validationResult,
        )
        val result = ColorSubmissionResult(
            wasAccepted = wasAccepted,
            discard = ::clearColorSubmissionResult,
        )
        _colorSubmissionResultFlow.value = result
    }

    private fun clearColorSubmissionResult() {
        _colorSubmissionResultFlow.value = null
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            coroutineScope: CoroutineScope,
            mediator: ColorInputMediator,
            eventStore: ColorInputEventStore,
            submitAction: ColorInputSubmitAction,
        ): ColorInputHexViewModel
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