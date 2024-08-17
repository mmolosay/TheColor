package io.github.mmolosay.thecolor.presentation.input.impl.rgb

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.presentation.ColorInputEvent
import io.github.mmolosay.thecolor.presentation.ColorInputEventStore
import io.github.mmolosay.thecolor.presentation.input.impl.ColorInputMediator
import io.github.mmolosay.thecolor.presentation.input.impl.ColorInputMediator.InputType
import io.github.mmolosay.thecolor.presentation.input.impl.ColorInputValidator
import io.github.mmolosay.thecolor.presentation.input.impl.SharingStartedEagerlyAnd
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldData
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldData.Text
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldViewModel
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldViewModel.Companion.updateWith
import io.github.thecolor.presentation.input.api.ColorInput
import io.github.thecolor.presentation.input.api.ColorInputState
import io.github.mmolosay.thecolor.presentation.input.impl.model.DataState
import io.github.mmolosay.thecolor.presentation.input.impl.model.FullData
import io.github.mmolosay.thecolor.presentation.input.impl.model.Update
import io.github.mmolosay.thecolor.presentation.input.impl.model.asDataState
import io.github.mmolosay.thecolor.presentation.input.impl.model.causedByUser
import io.github.mmolosay.thecolor.utils.onEachNotNull
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Named

internal typealias FullDataRgb = FullData<ColorInputRgbData, ColorInput.Rgb>

/**
 * Not a ViewModel-ViewModel in terms of Android development.
 * It doesn't derive from [androidx.lifecycle.ViewModel], so should only be used in "real" ViewModels
 * which do derive from Android-aware implementation.
 */
class ColorInputRgbViewModel @AssistedInject constructor(
    @Assisted private val coroutineScope: CoroutineScope,
    @Assisted private val mediator: ColorInputMediator,
    @Assisted private val eventStore: ColorInputEventStore,
    private val colorInputValidator: ColorInputValidator,
    @Named("uiDataUpdateDispatcher") private val uiDataUpdateDispatcher: CoroutineDispatcher,
) {

    private val rTextFieldVm = TextFieldViewModel(filterUserInput = ::filterUserInput)
    private val gTextFieldVm = TextFieldViewModel(filterUserInput = ::filterUserInput)
    private val bTextFieldVm = TextFieldViewModel(filterUserInput = ::filterUserInput)

    private val fullDataUpdateFlow: StateFlow<Update<FullDataRgb>?> =
        combine(
            rTextFieldVm.dataUpdatesFlow,
            gTextFieldVm.dataUpdatesFlow,
            bTextFieldVm.dataUpdatesFlow,
            ::combineTextInputUpdates,
        )
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

    init {
        collectMediatorUpdates()
    }

    private fun collectMediatorUpdates() {
        coroutineScope.launch(uiDataUpdateDispatcher) {
            mediator.rgbColorInputFlow.collect { input ->
                rTextFieldVm updateWith Text(input.r)
                gTextFieldVm updateWith Text(input.g)
                bTextFieldVm updateWith Text(input.b)
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

    private fun sendProceedEvent() {
        // TODO: repeated in ColorInputHexViewModel; reuse? base abstract class?
        coroutineScope.launch {
            eventStore.send(ColorInputEvent.Submit)
        }
    }

    private fun combineTextInputUpdates(
        r: Update<TextFieldData>?,
        g: Update<TextFieldData>?,
        b: Update<TextFieldData>?,
    ): Update<FullDataRgb>? {
        if (r == null || g == null || b == null) return null
        val coreData = ColorInputRgbData(
            rTextField = r.payload,
            gTextField = g.payload,
            bTextField = b.payload,
            submitColor = ::sendProceedEvent,
        )
        val colorInput = ColorInput.Rgb(
            r = r.payload.text.string,
            g = g.payload.text.string,
            b = b.payload.text.string,
        )
        val inputState = with(colorInputValidator) { colorInput.validate() }
        val fullData = io.github.mmolosay.thecolor.presentation.input.impl.rgb.FullDataRgb(
            coreData = coreData,
            colorInput = colorInput,
            colorInputState = inputState,
        )
        return fullData causedByUser listOf(r, g, b).any { it.causedByUser }
    }

    private fun onEachFullDataUpdate(update: Update<FullDataRgb>) {
        // don't synchronize this update with other Views to avoid update loop
        if (!update.causedByUser) return
        val parsedColor = (update.payload.colorInputState as? ColorInputState.Valid)?.color
        coroutineScope.launch(uiDataUpdateDispatcher) {
            mediator.send(color = parsedColor, from = InputType.Rgb)
        }
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