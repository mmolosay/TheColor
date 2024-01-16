package io.github.mmolosay.thecolor.presentation.home.input.hex

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.presentation.home.input.ColorInputFieldUiData
import io.github.mmolosay.thecolor.presentation.home.input.ColorInputFieldUiData.ViewData
import io.github.mmolosay.thecolor.presentation.home.input.ColorInputFieldViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ColorInputHexViewModel @Inject constructor() : ViewModel() {

    private val colorInputFieldViewModel =
        ColorInputFieldViewModel(
            processText = ::processInput,
        )

    val uiDataFlow: StateFlow<ColorInputHexUiData?> =
        colorInputFieldViewModel.uiDataFlow
            .map(::makeUiData)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null,
            )

    // TODO: try Factory for ViewModel with assisted ViewData in constructor
    fun init(viewData: ViewData) {
        colorInputFieldViewModel.init(viewData)
    }

    private fun processInput(input: String): String =
        input.take(MAX_SYMBOLS_IN_HEX_COLOR)

    private fun makeUiData(inputFieldUiData: ColorInputFieldUiData?): ColorInputHexUiData? {
        inputFieldUiData ?: return null
        return ColorInputHexUiData(inputFieldUiData)
    }

    private companion object {
        const val MAX_SYMBOLS_IN_HEX_COLOR = 6
    }
}