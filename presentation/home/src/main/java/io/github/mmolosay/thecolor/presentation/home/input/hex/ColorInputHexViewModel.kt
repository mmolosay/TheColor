package io.github.mmolosay.thecolor.presentation.home.input.hex

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.presentation.home.input.ColorInputFieldUiData
import io.github.mmolosay.thecolor.presentation.home.input.ColorInputFieldUiData.ViewData
import io.github.mmolosay.thecolor.presentation.home.input.ColorInputFieldViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel(assistedFactory = ColorInputHexViewModel.Factory::class)
class ColorInputHexViewModel @AssistedInject constructor(
    @Assisted viewData: ViewData,
) : ViewModel() {

    private val colorInputFieldViewModel =
        ColorInputFieldViewModel(
            viewData = viewData,
            processText = ::processInput,
        )

    val uiDataFlow =
        colorInputFieldViewModel.uiDataFlow
            .map(::makeUiData)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = makeUiData(colorInputFieldViewModel.uiDataFlow.value),
            )

    private fun processInput(input: String): String =
        input.take(MAX_SYMBOLS_IN_HEX_COLOR)

    private fun makeUiData(inputFieldUiData: ColorInputFieldUiData) =
        ColorInputHexUiData(inputFieldUiData)

    private companion object {
        const val MAX_SYMBOLS_IN_HEX_COLOR = 6
    }

    @AssistedFactory
    interface Factory {
        fun create(viewData: ViewData): ColorInputHexViewModel
    }
}