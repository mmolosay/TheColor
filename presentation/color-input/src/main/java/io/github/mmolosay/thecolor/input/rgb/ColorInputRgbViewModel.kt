package io.github.mmolosay.thecolor.input.rgb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.input.ColorInputMediator
import io.github.mmolosay.thecolor.input.InitialTextProvider
import io.github.mmolosay.thecolor.input.Update
import io.github.mmolosay.thecolor.input.field.TextFieldUiData
import io.github.mmolosay.thecolor.input.field.TextFieldUiData.Text
import io.github.mmolosay.thecolor.input.field.TextFieldViewModel
import io.github.mmolosay.thecolor.input.field.TextFieldViewModel.Companion.updateWith
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ColorInputRgbViewModel.Factory::class)
class ColorInputRgbViewModel @AssistedInject constructor(
    @Assisted viewData: ColorInputRgbViewData,
    initialTextProvider: InitialTextProvider,
    private val mediator: ColorInputMediator,
    private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val rTextInputVm =
        TextFieldViewModel(
            initialText = initialTextProvider.rgbR,
            viewData = viewData.rInputField,
            filterUserInput = ::filterUserInput,
        )
    private val gTextInputVm =
        TextFieldViewModel(
            initialText = initialTextProvider.rgbG,
            viewData = viewData.gInputField,
            filterUserInput = ::filterUserInput,
        )
    private val bTextInputVm =
        TextFieldViewModel(
            initialText = initialTextProvider.rgbB,
            viewData = viewData.bInputField,
            filterUserInput = ::filterUserInput,
        )

    val uiDataFlow = combine(
        rTextInputVm.uiDataUpdatesFlow,
        gTextInputVm.uiDataUpdatesFlow,
        bTextInputVm.uiDataUpdatesFlow,
    ) { r, g, b ->
        Update(
            data = makeUiData(r.data, g.data, b.data),
            causedByUser = listOf(r, g, b).any { it.causedByUser },
        )
    }
        .onEach(::onEachUiDataUpdate)
        .map { it.data }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = makeInitialUiData(),
        )

    init {
        collectMediatorUpdates()
    }

    private fun collectMediatorUpdates() {
        viewModelScope.launch(defaultDispatcher) {
            mediator.rgbStateFlow.collect { input ->
                rTextInputVm updateWith Text(input.r)
                gTextInputVm updateWith Text(input.g)
                bTextInputVm updateWith Text(input.b)
            }
        }
    }

    private fun onEachUiDataUpdate(update: Update<ColorInputRgbUiData>) {
        if (!update.causedByUser) return // don't synchronize this update with other Views
        val (uiData) = update
        val input = uiData.assembleColorInput()
        mediator.send(input)
    }

    private fun filterUserInput(input: String): Text =
        input
            .filter { it.isDigit() }
            .take(MAX_SYMBOLS_IN_RGB_COMPONENT)
            .let { string ->
                if (string.isEmpty()) return@let ""
                var int = string.toIntOrNull() ?: MIN_RGB_COMPONENT_VALUE // removes leading zeros
                while (int > MAX_RGB_COMPONENT_VALUE) // reduces int from right until it's in range
                    int /= 10
                int.toString()
            }
            .let { Text(it) }

    private fun makeInitialUiData() =
        makeUiData(
            rTextInputVm.uiDataUpdatesFlow.value.data,
            gTextInputVm.uiDataUpdatesFlow.value.data,
            bTextInputVm.uiDataUpdatesFlow.value.data,
        )

    private fun makeUiData(
        rTextField: TextFieldUiData,
        gTextField: TextFieldUiData,
        bTextField: TextFieldUiData,
    ) =
        ColorInputRgbUiData(rTextField, gTextField, bTextField)

    @AssistedFactory
    interface Factory {
        fun create(viewData: ColorInputRgbViewData): ColorInputRgbViewModel
    }

    private companion object {
        const val MAX_SYMBOLS_IN_RGB_COMPONENT = 3
        const val MIN_RGB_COMPONENT_VALUE = 0
        const val MAX_RGB_COMPONENT_VALUE = 255
    }
}