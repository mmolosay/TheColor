package io.github.mmolosay.thecolor.presentation.input.impl.hex

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.presentation.api.SimpleViewModel
import io.github.mmolosay.thecolor.presentation.input.api.ColorInput
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputEvent
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputEventStore
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputState
import io.github.mmolosay.thecolor.presentation.input.impl.ColorInputMediator
import io.github.mmolosay.thecolor.presentation.input.impl.ColorInputMediator.InputType
import io.github.mmolosay.thecolor.presentation.input.impl.ColorInputValidator
import io.github.mmolosay.thecolor.presentation.input.impl.SharingStartedEagerlyAnd
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldData
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldData.Text
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldViewModel
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldViewModel.Companion.updateWith
import io.github.mmolosay.thecolor.presentation.input.impl.model.ColorSubmissionResult
import io.github.mmolosay.thecolor.presentation.input.impl.model.DataState
import io.github.mmolosay.thecolor.presentation.input.impl.model.FullData
import io.github.mmolosay.thecolor.presentation.input.impl.model.Update
import io.github.mmolosay.thecolor.presentation.input.impl.model.asDataState
import io.github.mmolosay.thecolor.utils.onEachNotNull
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Named

internal typealias FullDataHex = FullData<ColorInputHexData, ColorInput.Hex>

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
    private val colorInputValidator: ColorInputValidator,
    @Named("defaultDispatcher") private val defaultDispatcher: CoroutineDispatcher,
    @Named("uiDataUpdateDispatcher") private val uiDataUpdateDispatcher: CoroutineDispatcher,
) : SimpleViewModel(coroutineScope) {

    private val textFieldVm = TextFieldViewModel(filterUserInput = ::filterUserInput)

    private val dataUpdateFlow = MutableStateFlow<Update<ColorInputHexData>?>(null)

    private val fullDataUpdateFlow: StateFlow<Update<FullDataHex>?> =
        dataUpdateFlow
            .map(::makeFullDataUpdate)
            .onEachNotNull(::onEachFullDataUpdate)
            .stateIn(
                scope = coroutineScope,
                started = SharingStartedEagerlyAnd(WhileSubscribed(5000)),
                initialValue = null,
            )

    val dataStateFlow: StateFlow<DataState<ColorInputHexData>> =
        fullDataUpdateFlow
            .map { update -> update?.payload?.coreData }
            .map { colorInputHexData -> colorInputHexData.asDataState() }
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
     * Transforms emissions of [textFieldVm] into updates of [ColorInputHexData].
     * Collects results in [dataUpdateFlow].
     * This allows having [MutableStateFlow] that derives from another flow.
     */
    private fun collectTextFieldUpdates() {
        coroutineScope.launch(defaultDispatcher) {
            textFieldVm.dataUpdatesFlow
                .map(::makeDataUpdate)
                .collect(dataUpdateFlow)
        }
    }

    private fun collectMediatorUpdates() {
        coroutineScope.launch(uiDataUpdateDispatcher) {
            mediator.hexColorInputFlow.collect { input ->
                textFieldVm updateWith Text(input.string)
            }
        }
    }

    private fun filterUserInput(input: String): Text =
        input
            .uppercase()
            .filter { it.isDigit() || it in 'A'..'F' }
            .take(MAX_SYMBOLS_IN_HEX_COLOR)
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
        textFieldUpdate: Update<TextFieldData>?,
    ): Update<ColorInputHexData>? {
        textFieldUpdate ?: return null
        val currentData = dataUpdateFlow.value?.payload
        val newData = if (currentData != null) {
            currentData.copy(
                textField = textFieldUpdate.payload,
            )
        } else {
            ColorInputHexData(
                textField = textFieldUpdate.payload,
                submitColor = ::sendSubmitEvent,
            )
        }
        return Update(payload = newData, causedByUser = textFieldUpdate.causedByUser)
    }

    private fun makeFullDataUpdate(
        coreDataUpdate: Update<ColorInputHexData>?,
    ): Update<FullDataHex>? {
        val coreData = coreDataUpdate?.payload ?: return null
        val colorInput = ColorInput.Hex(string = coreData.textField.text.string)
        val inputState = with(colorInputValidator) { colorInput.validate() }
        val fullData = FullData(
            coreData = coreData,
            colorInput = colorInput,
            colorInputState = inputState,
        )
        return Update(payload = fullData, causedByUser = coreDataUpdate.causedByUser)
    }

    private fun onEachFullDataUpdate(update: Update<FullDataHex>) {
        // don't synchronize this update with other Views to avoid update loop
        if (!update.causedByUser) return
        val parsedColor = (update.payload.colorInputState as? ColorInputState.Valid)?.color
        coroutineScope.launch(uiDataUpdateDispatcher) {
            mediator.send(color = parsedColor, from = InputType.Hex)
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
        ): ColorInputHexViewModel
    }

    private companion object {
        const val MAX_SYMBOLS_IN_HEX_COLOR = 6
    }
}