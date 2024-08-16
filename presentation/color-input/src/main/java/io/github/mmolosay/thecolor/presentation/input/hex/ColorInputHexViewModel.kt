package io.github.mmolosay.thecolor.presentation.input.hex

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.presentation.ColorInputEvent
import io.github.mmolosay.thecolor.presentation.ColorInputEventStore
import io.github.mmolosay.thecolor.presentation.input.ColorInputMediator
import io.github.mmolosay.thecolor.presentation.input.ColorInputMediator.InputType
import io.github.mmolosay.thecolor.presentation.input.ColorInputValidator
import io.github.mmolosay.thecolor.presentation.input.SharingStartedEagerlyAnd
import io.github.mmolosay.thecolor.presentation.input.field.TextFieldData
import io.github.mmolosay.thecolor.presentation.input.field.TextFieldData.Text
import io.github.mmolosay.thecolor.presentation.input.field.TextFieldViewModel
import io.github.mmolosay.thecolor.presentation.input.field.TextFieldViewModel.Companion.updateWith
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputState
import io.github.mmolosay.thecolor.presentation.input.model.DataState
import io.github.mmolosay.thecolor.presentation.input.model.Update
import io.github.mmolosay.thecolor.presentation.input.model.asDataState
import io.github.mmolosay.thecolor.presentation.input.model.map
import io.github.mmolosay.thecolor.utils.onEachNotNull
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Named

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

    private val textFieldVm =
        TextFieldViewModel(
            filterUserInput = ::filterUserInput,
        )

    val dataStateFlow: StateFlow<DataState<ColorInputHexData>> =
        textFieldVm.dataUpdatesFlow
            .map { it?.map(::makeData) }
            .onEachNotNull(::onEachUiDataUpdate)
            .map { it?.data.asDataState() }
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

    private fun onEachUiDataUpdate(update: Update<ColorInputHexData>) {
        if (!update.causedByUser) return // don't synchronize this update with other Views TODO: refine comment
        val input = update.data.assembleColorInput()
        val inputState = with(colorInputValidator) { input.validate() }
        val parsedColor = (inputState as? ColorInputState.Valid)?.color
        coroutineScope.launch(uiDataUpdateDispatcher) {
            mediator.send(color = parsedColor, inputType = InputType.Hex)
        }
    }

    private fun filterUserInput(input: String): Text =
        input
            .uppercase()
            .filter { it.isDigit() || it in 'A'..'F' }
            .take(MAX_SYMBOLS_IN_HEX_COLOR)
            .let { Text(it) }

    private fun sendProceedEvent() {
        coroutineScope.launch {
            eventStore.send(ColorInputEvent.Submit)
        }
    }

    private fun makeData(textField: TextFieldData) =
        ColorInputHexData(
            textField = textField,
            submitColor = ::sendProceedEvent,
        )

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