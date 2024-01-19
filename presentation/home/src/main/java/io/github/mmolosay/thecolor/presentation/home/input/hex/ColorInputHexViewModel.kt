package io.github.mmolosay.thecolor.presentation.home.input.hex

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.presentation.color.ColorInput
import io.github.mmolosay.thecolor.presentation.home.input.ColorInputMediator
import io.github.mmolosay.thecolor.presentation.home.input.InitialTextProvider
import io.github.mmolosay.thecolor.presentation.home.input.Update
import io.github.mmolosay.thecolor.presentation.home.input.field.ColorInputFieldUiData
import io.github.mmolosay.thecolor.presentation.home.input.field.ColorInputFieldUiData.Text
import io.github.mmolosay.thecolor.presentation.home.input.field.ColorInputFieldUiData.ViewData
import io.github.mmolosay.thecolor.presentation.home.input.field.ColorInputFieldViewModel
import io.github.mmolosay.thecolor.presentation.home.input.field.ColorInputFieldViewModel.ColorInputReducer
import io.github.mmolosay.thecolor.presentation.home.input.map
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ColorInputHexViewModel.Factory::class)
class ColorInputHexViewModel @AssistedInject constructor(
    @Assisted viewData: ViewData,
    initialTextProvider: InitialTextProvider,
    private val mediator: ColorInputMediator,
    private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val inputFieldViewModel =
        ColorInputFieldViewModel(
            initialText = initialTextProvider.hex,
            viewData = viewData,
            filterUserInput = ::filterUserInput,
        )

    private val colorInputReducer = ColorInputReducer<ColorInput.Hex> { input ->
        Text(input.string)
    }

    val uiDataFlow =
        inputFieldViewModel.uiDataUpdatesFlow
            .map { it.map(::makeUiData) }
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
            mediator.hexStateFlow.collect { state ->
                with(colorInputReducer) { inputFieldViewModel apply state }
            }
        }
    }

    private fun onEachUiDataUpdate(update: Update<ColorInputHexUiData>) {
        if (!update.causedByUser) return // don't synchronize this update with other Views
        val (uiData) = update
        val input = uiData.assembleColorInput()
        mediator.send(input)
    }

    private fun filterUserInput(input: String): Text =
        input
            .filter { it.isDigit() || it in 'A'..'F' }
            .take(MAX_SYMBOLS_IN_HEX_COLOR)
            .let { Text(it) }

    private fun makeUiData(inputField: ColorInputFieldUiData) =
        ColorInputHexUiData(inputField)

    private fun makeInitialUiData() =
        makeUiData(inputFieldViewModel.uiDataUpdatesFlow.value.data)

    private companion object {
        const val MAX_SYMBOLS_IN_HEX_COLOR = 6
    }

    @AssistedFactory
    interface Factory {
        fun create(viewData: ViewData): ColorInputHexViewModel
    }
}