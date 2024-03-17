package io.github.mmolosay.thecolor.presentation.input.rgb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.presentation.ColorInputEvent
import io.github.mmolosay.thecolor.presentation.ColorInputEventStore
import io.github.mmolosay.thecolor.presentation.input.ColorInputMediator
import io.github.mmolosay.thecolor.presentation.input.SharingStartedEagerlyAnd
import io.github.mmolosay.thecolor.presentation.input.field.TextFieldData
import io.github.mmolosay.thecolor.presentation.input.field.TextFieldData.Text
import io.github.mmolosay.thecolor.presentation.input.field.TextFieldViewModel
import io.github.mmolosay.thecolor.presentation.input.field.TextFieldViewModel.Companion.updateWith
import io.github.mmolosay.thecolor.presentation.input.model.DataState
import io.github.mmolosay.thecolor.presentation.input.model.Update
import io.github.mmolosay.thecolor.presentation.input.model.asDataState
import io.github.mmolosay.thecolor.presentation.input.model.causedByUser
import io.github.mmolosay.thecolor.utils.onEachNotNull
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class ColorInputRgbViewModel @Inject constructor(
    private val mediator: ColorInputMediator,
    private val eventStore: ColorInputEventStore,
    @Named("uiDataUpdateDispatcher") private val uiDataUpdateDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val rTextInputVm =
        TextFieldViewModel(
            filterUserInput = ::filterUserInput,
        )
    private val gTextInputVm =
        TextFieldViewModel(
            filterUserInput = ::filterUserInput,
        )
    private val bTextInputVm =
        TextFieldViewModel(
            filterUserInput = ::filterUserInput,
        )

    val dataStateFlow: StateFlow<DataState<ColorInputRgbData>> = combine(
        rTextInputVm.dataUpdatesFlow,
        gTextInputVm.dataUpdatesFlow,
        bTextInputVm.dataUpdatesFlow,
        ::combineTextInputUpdates,
    )
        .onEachNotNull(::onEachDataUpdate)
        .map { it?.data.asDataState() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStartedEagerlyAnd(WhileSubscribed(5000)),
            initialValue = DataState.BeingInitialized,
        )

    init {
        collectMediatorUpdates()
    }

    private fun collectMediatorUpdates() {
        viewModelScope.launch(uiDataUpdateDispatcher) {
            mediator.rgbColorInputFlow.collect { input ->
                rTextInputVm updateWith Text(input.r)
                gTextInputVm updateWith Text(input.g)
                bTextInputVm updateWith Text(input.b)
            }
        }
    }

    private fun combineTextInputUpdates(
        r: Update<TextFieldData>?,
        g: Update<TextFieldData>?,
        b: Update<TextFieldData>?,
    ): Update<ColorInputRgbData>? {
        if (r == null || g == null || b == null) return null
        val data = makeData(r.data, g.data, b.data)
        return data causedByUser listOf(r, g, b).any { it.causedByUser }
    }

    private fun onEachDataUpdate(update: Update<ColorInputRgbData>) {
        if (!update.causedByUser) return // don't synchronize this update with other Views
        val uiData = update.data
        val input = uiData.assembleColorInput()
        viewModelScope.launch(uiDataUpdateDispatcher) {
            mediator.send(input)
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

    private fun sendProceedEvent() {
        // TODO: repeated in ColorInputHexViewModel; reuse? base abstract class?
        viewModelScope.launch {
            eventStore.send(ColorInputEvent.Submit)
        }
    }

    private fun makeData(
        rTextField: TextFieldData,
        gTextField: TextFieldData,
        bTextField: TextFieldData,
    ) =
        ColorInputRgbData(
            rTextField = rTextField,
            gTextField = gTextField,
            bTextField = bTextField,
            submitColor = ::sendProceedEvent,
        )

    private companion object {
        const val MAX_SYMBOLS_IN_RGB_COMPONENT = 3
        const val MIN_RGB_COMPONENT_VALUE = 0
        const val MAX_RGB_COMPONENT_VALUE = 255
    }
}