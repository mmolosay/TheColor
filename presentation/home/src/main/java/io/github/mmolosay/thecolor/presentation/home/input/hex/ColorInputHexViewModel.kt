package io.github.mmolosay.thecolor.presentation.home.input.hex

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.presentation.color.ColorPrototype
import io.github.mmolosay.thecolor.presentation.home.input.ColorInputMediator
import io.github.mmolosay.thecolor.presentation.home.input.field.ColorInputFieldUiData
import io.github.mmolosay.thecolor.presentation.home.input.field.ColorInputFieldUiData.Text
import io.github.mmolosay.thecolor.presentation.home.input.field.ColorInputFieldUiData.ViewData
import io.github.mmolosay.thecolor.presentation.home.input.field.ColorInputFieldViewModel
import io.github.mmolosay.thecolor.presentation.home.input.field.ColorInputFieldViewModel.State
import io.github.mmolosay.thecolor.presentation.home.input.field.ColorInputFieldViewModel.StateReducer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ColorInputHexViewModel.Factory::class)
class ColorInputHexViewModel @AssistedInject constructor(
    @Assisted viewData: ViewData,
    private val mediator: ColorInputMediator,
    private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val inputFieldViewModel =
        ColorInputFieldViewModel(
            viewData = viewData,
            filterUserInput = ::filterUserInput,
        )

    private val inputFieldStateReducer = StateReducer<ColorPrototype.Hex> { color ->
        Text(color.value.orEmpty())
    }

    val uiDataFlow =
        inputFieldViewModel.uiDataFlow
            .map(::makeUiData)
            .onEach { uiData ->
                val state = if (uiData.inputField.text.string.isNotEmpty()) {
                    val prototype = uiData.assembleColorPrototype()
                    State.Populated(prototype)
                } else {
                    State.Empty
                }
                mediator.send(state)
            }
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
            mediator.hexStateFlow.collect { state ->
                with(inputFieldStateReducer) { inputFieldViewModel apply state }
            }
        }
    }

    private fun filterUserInput(input: String): Text =
        input
            .filter { it.isDigit() || it in 'A'..'F' }
            .take(MAX_SYMBOLS_IN_HEX_COLOR)
            .let { Text(it) }

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