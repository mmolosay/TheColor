package io.github.mmolosay.thecolor.presentation.home.input.hex

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.presentation.home.input.ColorInputMediator
import io.github.mmolosay.thecolor.presentation.home.input.field.ColorInputFieldUiData
import io.github.mmolosay.thecolor.presentation.home.input.field.ColorInputFieldUiData.ViewData
import io.github.mmolosay.thecolor.presentation.home.input.field.ColorInputFieldViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ColorInputHexViewModel.Factory::class)
class ColorInputHexViewModel @AssistedInject constructor(
    @Assisted viewData: ViewData,
    private val mediator: ColorInputMediator,
) : ViewModel() {

    private val inputFieldViewModel =
        ColorInputFieldViewModel(
            viewData = viewData,
            processText = ::processInput,
        )

    val uiDataFlow =
        inputFieldViewModel.uiDataFlow
            .map(::makeUiData)
            .onEach { uiData ->
                val prototype = uiData.assembleColorPrototype()
                mediator.update(prototype)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = makeInitialUiData(),
            )

    init {
        collectMediatorColorFlow()
    }

    private fun collectMediatorColorFlow() {
        viewModelScope.launch {
            mediator.hexFlow.collect { color ->
                val newText = color.value.orEmpty()
                uiDataFlow.value.inputField.onTextChange(newText)
            }
        }
    }

    private fun processInput(text: String): String =
        text
            .filter { it.isDigit() || it in 'A'..'F' }
            .take(MAX_SYMBOLS_IN_HEX_COLOR)

    private fun makeUiData(inputField: ColorInputFieldUiData) =
        ColorInputHexUiData(inputField)

    private fun makeInitialUiData() =
        makeUiData(inputFieldViewModel.uiDataFlow.value)

    private companion object {
        const val MAX_SYMBOLS_IN_HEX_COLOR = 6
    }

    @AssistedFactory
    interface Factory {
        fun create(viewData: ViewData): ColorInputHexViewModel
    }
}