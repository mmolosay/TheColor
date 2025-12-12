package io.github.mmolosay.thecolor.presentation.input.rgb

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.model.ColorConstants
import io.github.mmolosay.thecolor.domain.repository.DefaultUserPreferences
import io.github.mmolosay.thecolor.domain.repository.UserPreferencesRepository
import io.github.mmolosay.thecolor.presentation.common.ImmediateRelay
import io.github.mmolosay.thecolor.presentation.common.viewmodel.SimpleViewModel
import io.github.mmolosay.thecolor.presentation.common.viewmodel.ViewModelCoroutineScope
import io.github.mmolosay.thecolor.presentation.input.ColorInputEventStore
import io.github.mmolosay.thecolor.presentation.input.ColorInputMediator
import io.github.mmolosay.thecolor.presentation.input.ColorInputValidator
import io.github.mmolosay.thecolor.presentation.input.model.ColorInput
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmitAction
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputValidationResult
import io.github.mmolosay.thecolor.presentation.input.model.ColorSubmissionResult
import io.github.mmolosay.thecolor.presentation.input.model.DataState
import io.github.mmolosay.thecolor.presentation.input.model.getColorOrNull
import io.github.mmolosay.thecolor.presentation.input.plus
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldViewModel
import io.github.mmolosay.thecolor.presentation.input.textfield.updateText
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Named
import io.github.mmolosay.thecolor.domain.model.ColorInputType as DomainColorInputType

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
    @Assisted private val mediator: ColorInputMediator,
    @Assisted private val eventStore: ColorInputEventStore,
    @Assisted private val submitAction: ColorInputSubmitAction,
    private val textFieldViewModelFactory: TextFieldViewModel.Factory,
    private val colorInputValidator: ColorInputValidator,
    private val userPreferencesRepository: UserPreferencesRepository,
    @Named("defaultDispatcher") private val defaultDispatcher: CoroutineDispatcher,
    @Named("uiDataUpdateDispatcher") private val uiDataUpdateDispatcher: CoroutineDispatcher,
) : SimpleViewModel(coroutineScope) {

    private val rTextFieldVm = createTextFieldViewModel()
    private val gTextFieldVm = createTextFieldViewModel()
    private val bTextFieldVm = createTextFieldViewModel()

    val dataStateFlow: StateFlow<DataState<ColorInputRgbData>> =
        combine(
            rTextFieldVm.dataFlow,
            gTextFieldVm.dataFlow,
            bTextFieldVm.dataFlow,
            userPreferencesRepository.flowOfSmartBackspace.map { it ?: DefaultUserPreferences.SmartBackspace },
        ) { r, g, b, smartBackspace ->
            val colorInput = ColorInput.Rgb(
                r = r.text.data.string,
                g = g.text.data.string,
                b = b.text.data.string,
            )
            val validationResult = with(colorInputValidator) { colorInput.validate() }
            FullData(
                rTextField = r,
                gTextField = g,
                bTextField = b,
                submitInput = { submitInput(colorInput, validationResult) },
                isSmartBackspaceEnabled = smartBackspace.enabled,
                colorInput = colorInput,
                colorInputValidationResult = validationResult,
            )
        }
            .onEach { fullData ->
                // don't synchronize this data with other Views to avoid update loop
                val isAnyCausedByUser =
                    listOf(fullData.rTextField, fullData.gTextField, fullData.bTextField)
                        .any { it.text.causedByUser }
                if (!isAnyCausedByUser) return@onEach // none caused by user
                val parsedColor = fullData.colorInputValidationResult.getColorOrNull()
                mediator.send(color = parsedColor, from = DomainColorInputType.Rgb)
            }
            .map { fullData -> fullData.reduce() }
            .map { data -> DataState(data) }
            .flowOn(defaultDispatcher)
            .stateIn(
                scope = coroutineScope,
                started = SharingStarted.Eagerly + SharingStarted.WhileSubscribed(5000), // start eagerly to pre-compute first value before UI starts collecting
                initialValue = DataState.BeingInitialized,
            )

    private val colorSubmissionResultRelay = ImmediateRelay<ColorSubmissionResult>()
    val colorSubmissionResultFlow = colorSubmissionResultRelay.flowForView

    init {
        collectMediatorUpdates()
    }

    private fun collectMediatorUpdates() {
        coroutineScope.launch(uiDataUpdateDispatcher) {
            mediator.rgbColorInputFlow.collect { input ->
                rTextFieldVm updateText TextFieldData.Text(input.r)
                gTextFieldVm updateText TextFieldData.Text(input.g)
                bTextFieldVm updateText TextFieldData.Text(input.b)
            }
        }
    }

    private fun filterUserInput(input: String): TextFieldData.Text =
        input
            .filter { it.isDigit() }
            .take(3) // rgb component can be up to 3 digits long
            .let { string ->
                if (string.isEmpty()) return@let ""
                val rgbComponentMinValue = ColorConstants.RgbColorComponentIntRange.first
                val rgbComponentMaxValue = ColorConstants.RgbColorComponentIntRange.last
                var int = string.toIntOrNull() ?: rgbComponentMinValue // remove leading zeros
                // reduce int from right until it's in range
                while (int > rgbComponentMaxValue) {
                    int /= 10
                }
                int.toString()
            }
            .let { TextFieldData.Text(it) }

    private fun submitInput(
        colorInput: ColorInput.Rgb,
        validationResult: ColorInputValidationResult,
    ) {
        val wasAccepted = submitAction.invoke(
            colorInput = colorInput,
            validationResult = validationResult,
        )
        val result = ColorSubmissionResult(wasAccepted)
        coroutineScope.launch {
            colorSubmissionResultRelay.send(result)
        }
    }

    private fun createTextFieldViewModel(): TextFieldViewModel =
        textFieldViewModelFactory.create(
            coroutineScope = ViewModelCoroutineScope(parent = coroutineScope),
            filterUserInput = ::filterUserInput,
            enableClearTextFeature = false,
        )

    override fun dispose() {
        super.dispose()
        rTextFieldVm.dispose()
        gTextFieldVm.dispose()
        bTextFieldVm.dispose()
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            coroutineScope: CoroutineScope,
            mediator: ColorInputMediator,
            eventStore: ColorInputEventStore,
            submitAction: ColorInputSubmitAction,
        ): ColorInputRgbViewModel
    }
}

/**
 * Couples data which is exposed from the ViewModel with various values that are related to it:
 * derived from the exposed data, or used to produce it.
 *
 * @param colorInput contains data from all of the text fields.
 * @param colorInputValidationResult is a result of [colorInput] validation.
 */
private data class FullData(
    val rTextField: TextFieldData,
    val gTextField: TextFieldData,
    val bTextField: TextFieldData,
    val submitInput: () -> Unit,
    val isSmartBackspaceEnabled: Boolean,
    val colorInput: ColorInput.Rgb,
    val colorInputValidationResult: ColorInputValidationResult,
)

/**
 * Reduces [FullData] to the [ColorInputRgbData] which is exposed from the ViewModel.
 */
private fun FullData.reduce(): ColorInputRgbData =
    ColorInputRgbData(
        rTextField = rTextField,
        gTextField = gTextField,
        bTextField = bTextField,
        submitInput = submitInput,
        isSmartBackspaceEnabled = isSmartBackspaceEnabled,
    )