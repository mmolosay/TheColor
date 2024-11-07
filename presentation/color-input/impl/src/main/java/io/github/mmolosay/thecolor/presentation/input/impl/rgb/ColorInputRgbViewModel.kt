package io.github.mmolosay.thecolor.presentation.input.impl.rgb

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.presentation.api.SimpleViewModel
import io.github.mmolosay.thecolor.presentation.input.api.ColorInput
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputEvent
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputEventStore
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputState
import io.github.mmolosay.thecolor.presentation.input.impl.ColorInputMediator
import io.github.mmolosay.thecolor.presentation.input.impl.ColorInputValidator
import io.github.mmolosay.thecolor.presentation.input.impl.SharingStartedEagerlyAnd
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldData
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldData.Text
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldViewModel
import io.github.mmolosay.thecolor.presentation.input.impl.field.updateText
import io.github.mmolosay.thecolor.presentation.input.impl.model.ColorSubmissionResult
import io.github.mmolosay.thecolor.presentation.input.impl.model.DataState
import io.github.mmolosay.thecolor.presentation.input.impl.model.FullData
import io.github.mmolosay.thecolor.presentation.input.impl.model.Update
import io.github.mmolosay.thecolor.presentation.input.impl.model.asDataState
import io.github.mmolosay.thecolor.presentation.input.impl.model.causedByUser
import io.github.mmolosay.thecolor.utils.onEachNotNull
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Named
import io.github.mmolosay.thecolor.domain.model.ColorInputType as DomainColorInputType

internal typealias FullDataRgb = FullData<ColorInputRgbData, ColorInput.Rgb>

/**
 * Handles presentation logic of the 'RGB Color Preview' feature.
 *
 * Unlike typical `ViewModel`s, it doesn't derive from Google's [ViewModel][androidx.lifecycle.ViewModel],
 * thus cannot be instantiated using [ViewModelProvider][androidx.lifecycle.ViewModelProvider].
 *
 * Instead, it can be created within "simple" `ViewModel` or Google's `ViewModel`.
 */
// TODO: replicates ColorInputHexViewModel:
//  extract and reuse via composition? base abstract class via inheritance?
class ColorInputRgbViewModel @AssistedInject constructor(
    @Assisted coroutineScope: CoroutineScope,
    @Assisted private val mediator: ColorInputMediator,
    @Assisted private val eventStore: ColorInputEventStore,
    private val colorInputValidator: ColorInputValidator,
    @Named("defaultDispatcher") private val defaultDispatcher: CoroutineDispatcher,
    @Named("uiDataUpdateDispatcher") private val uiDataUpdateDispatcher: CoroutineDispatcher,
) : SimpleViewModel(coroutineScope) {

    private val rTextFieldVm = TextFieldViewModel(filterUserInput = ::filterUserInput)
    private val gTextFieldVm = TextFieldViewModel(filterUserInput = ::filterUserInput)
    private val bTextFieldVm = TextFieldViewModel(filterUserInput = ::filterUserInput)

    private val dataUpdateFlow = MutableStateFlow<Update<ColorInputRgbData>?>(null)

    private val fullDataUpdateFlow: StateFlow<Update<FullDataRgb>?> =
        dataUpdateFlow
            .map(::makeFullDataUpdate)
            .onEachNotNull(::onEachFullDataUpdate)
            .stateIn(
                scope = coroutineScope,
                started = SharingStartedEagerlyAnd(WhileSubscribed(5000)),
                initialValue = null,
            )

    val dataStateFlow: StateFlow<DataState<ColorInputRgbData>> =
        fullDataUpdateFlow
            .map { update -> update?.payload?.coreData }
            .map { colorInputRgbData -> colorInputRgbData.asDataState() }
            .stateIn(
                scope = coroutineScope,
                started = SharingStartedEagerlyAnd(WhileSubscribed(5000)),
                initialValue = DataState.BeingInitialized,
            )

    private val _colorSubmissionResultFlow = MutableStateFlow<ColorSubmissionResult?>(null)
    val colorSubmissionResultFlow = _colorSubmissionResultFlow.asStateFlow()

    init {
        collectTextFieldUpdates()
        collectMediatorUpdates()
    }

    /**
     * Transforms emissions of [TextFieldViewModel]s into updates of [ColorInputRgbData].
     * Collects results in [dataUpdateFlow].
     * This allows having [MutableStateFlow] that derives from another flow.
     */
    private fun collectTextFieldUpdates() {
        coroutineScope.launch(defaultDispatcher) {
            combine(
                rTextFieldVm.dataUpdatesFlow,
                gTextFieldVm.dataUpdatesFlow,
                bTextFieldVm.dataUpdatesFlow,
                ::makeDataUpdate,
            )
                .collect(dataUpdateFlow)
        }
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
            .take(MAX_SYMBOLS_IN_RGB_COMPONENT)
            .let { string ->
                if (string.isEmpty()) return@let ""
                var int = string.toIntOrNull() ?: MIN_RGB_COMPONENT_VALUE // remove leading zeros
                while (int > MAX_RGB_COMPONENT_VALUE) // reduce int from right until it's in range
                    int /= 10
                int.toString()
            }
            .let { Text(it) }

    private fun sendSubmitEvent() {
        val data = requireNotNull(fullDataUpdateFlow.value?.payload)
        coroutineScope.launch(defaultDispatcher) {
            val event = ColorInputEvent.Submit(
                colorInput = data.colorInput,
                colorInputState = data.colorInputState,
                onConsumed = ::onSubmitEventConsumed,
            )
            eventStore.send(event)
        }
    }

    private fun makeDataUpdate(
        r: Update<TextFieldData>?,
        g: Update<TextFieldData>?,
        b: Update<TextFieldData>?,
    ): Update<ColorInputRgbData>? {
        if (r == null || g == null || b == null) return null
        val currentData = dataUpdateFlow.value?.payload
        val newData = if (currentData != null) {
            currentData.copy(
                rTextField = r.payload,
                gTextField = g.payload,
                bTextField = b.payload,
            )
        } else {
            ColorInputRgbData(
                rTextField = r.payload,
                gTextField = g.payload,
                bTextField = b.payload,
                submitColor = ::sendSubmitEvent,
            )
        }
        return newData causedByUser listOf(r, g, b).any { it.causedByUser }
    }

    private fun makeFullDataUpdate(
        coreDataUpdate: Update<ColorInputRgbData>?,
    ): Update<FullDataRgb>? {
        val coreData = coreDataUpdate?.payload ?: return null
        val colorInput = ColorInput.Rgb(
            r = coreData.rTextField.text.string,
            g = coreData.gTextField.text.string,
            b = coreData.bTextField.text.string,
        )
        val inputState = with(colorInputValidator) { colorInput.validate() }
        val fullData = FullDataRgb(
            coreData = coreData,
            colorInput = colorInput,
            colorInputState = inputState,
        )
        return Update(payload = fullData, causedByUser = coreDataUpdate.causedByUser)
    }

    private fun onEachFullDataUpdate(update: Update<FullDataRgb>) {
        // don't synchronize this update with other Views to avoid update loop
        if (!update.causedByUser) return
        val parsedColor = (update.payload.colorInputState as? ColorInputState.Valid)?.color
        coroutineScope.launch(uiDataUpdateDispatcher) {
            mediator.send(color = parsedColor, from = DomainColorInputType.Rgb)
        }
    }

    private fun onSubmitEventConsumed(wasAccepted: Boolean) {
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
        ): ColorInputRgbViewModel
    }

    private companion object {
        const val MAX_SYMBOLS_IN_RGB_COMPONENT = 3
        const val MIN_RGB_COMPONENT_VALUE = 0
        const val MAX_RGB_COMPONENT_VALUE = 255
    }
}