package io.github.mmolosay.thecolor.presentation.input.impl.hex

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Named

internal typealias FullDataHex = FullData<ColorInputHexData, ColorInput.Hex>

/**
 * Not a ViewModel-ViewModel in terms of Android development.
 * It doesn't derive from [androidx.lifecycle.ViewModel], so should only be used in "real" ViewModels
 * which do derive from Android-aware implementation.
 */
class ColorInputHexViewModel @AssistedInject constructor(
    @Assisted private val coroutineScope: CoroutineScope,
    @Assisted private val mediator: ColorInputMediator,
    @Assisted private val eventStore: ColorInputEventStore,
    private val colorInputValidator: ColorInputValidator,
    @Named("uiDataUpdateDispatcher") private val uiDataUpdateDispatcher: CoroutineDispatcher,
) {

    private val textFieldVm = TextFieldViewModel(filterUserInput = ::filterUserInput)

    private val colorSubmissionResultFlow =
        MutableStateFlow<ColorInputHexData.ColorSubmissionResult?>(null)

    private val fullDataUpdateFlow: StateFlow<Update<FullDataHex>?> =
        combine(
            textFieldVm.dataUpdatesFlow,
            colorSubmissionResultFlow,
            transform = ::makeFullDataUpdate,
        )
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

    init {
        collectMediatorUpdates()
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
        coroutineScope.launch {
            val event = ColorInputEvent.Submit(
                colorInput = data.colorInput,
                colorInputState = data.colorInputState,
                onConsumed = ::onSubmitEventConsumed,
            )
            eventStore.send(event)
        }
    }

    private fun makeFullDataUpdate(
        textFieldUpdate: Update<TextFieldData>?,
        colorSubmissionResult: ColorInputHexData.ColorSubmissionResult?,
    ): Update<FullDataHex>? {
        textFieldUpdate ?: return null
        val textField = textFieldUpdate.payload
        val coreData = run {
            val currentCoreData = fullDataUpdateFlow.value?.payload?.coreData
            if (currentCoreData != null) {
                currentCoreData.copy(
                    textField = textField,
                    colorSubmissionResult = colorSubmissionResult,
                )
            } else {
                ColorInputHexData(
                    textField = textField,
                    submitColor = ::sendSubmitEvent,
                    colorSubmissionResult = colorSubmissionResult,
                )
            }
        }
        val colorInput = ColorInput.Hex(string = textField.text.string)
        val inputState = with(colorInputValidator) { colorInput.validate() }
        val fullData = FullData(
            coreData = coreData,
            colorInput = colorInput,
            colorInputState = inputState,
        )
        return Update(payload = fullData, causedByUser = textFieldUpdate.causedByUser)
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
        val result = ColorInputHexData.ColorSubmissionResult(
            wasAccepted = wasAccepted,
            discard = ::clearColorSubmissionResult,
        )
        colorSubmissionResultFlow.value = result
    }

    private fun clearColorSubmissionResult() {
        colorSubmissionResultFlow.value = null
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