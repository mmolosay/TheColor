package io.github.mmolosay.thecolor.presentation.input.impl.rgb

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.model.ColorConstants
import io.github.mmolosay.thecolor.domain.repository.DefaultUserPreferences
import io.github.mmolosay.thecolor.domain.repository.UserPreferencesRepository
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
import io.github.mmolosay.thecolor.presentation.input.impl.model.Update
import io.github.mmolosay.thecolor.presentation.input.impl.plus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
class ColorInputRgbViewModel @AssistedInject internal constructor(
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
            rTextFieldVm.dataUpdatesFlow,
            gTextFieldVm.dataUpdatesFlow,
            bTextFieldVm.dataUpdatesFlow,
            userPreferencesRepository.flowOfSmartBackspace.map { it ?: DefaultUserPreferences.SmartBackspace },
        ) { rUpdate, gUpdate, bUpdate, smartBackspace ->
            val colorInput = ColorInput.Rgb(
                r = rUpdate.payload.text.string,
                g = gUpdate.payload.text.string,
                b = bUpdate.payload.text.string,
            )
            val validationResult = with(colorInputValidator) { colorInput.validate() }
            val fullData = FullData(
                rTextField = rUpdate.payload,
                gTextField = gUpdate.payload,
                bTextField = bUpdate.payload,
                submitInput = { submitInput(colorInput, validationResult) },
                isSmartBackspaceEnabled = smartBackspace.enabled,
                colorInput = colorInput,
                colorInputValidationResult = validationResult,
            )
            val anyCausedByUser = listOf(rUpdate, gUpdate, bUpdate).any { it.causedByUser }
            Update(payload = fullData, causedByUser = anyCausedByUser)
        }
            .onEach { fullDataUpdate ->
                // don't synchronize this update with other Views to avoid update loop
                if (!fullDataUpdate.causedByUser) return@onEach
                val parsedColor = fullDataUpdate.payload.colorInputValidationResult.getColorOrNull()
                mediator.send(color = parsedColor, from = DomainColorInputType.Rgb)
            }
            .map { fullDataUpdate -> fullDataUpdate.payload }
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
            mediator.rgbColorInputFlow.collect { input ->
                rTextFieldVm updateText Text(input.r)
                gTextFieldVm updateText Text(input.g)
                bTextFieldVm updateText Text(input.b)
            }
        }
    }

    private fun filterUserInput(input: String): Text =
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
            .let { Text(it) }

    private fun submitInput(
        colorInput: ColorInput.Rgb,
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

    private fun createTextFieldViewModel(): TextFieldViewModel =
        textFieldViewModelFactory.create(
            coroutineScope = ViewModelCoroutineScope(parent = coroutineScope),
            filterUserInput = ::filterUserInput,
            enableClearTextFeature = false,
        )

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