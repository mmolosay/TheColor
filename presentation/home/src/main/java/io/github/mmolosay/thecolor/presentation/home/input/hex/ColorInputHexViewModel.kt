package io.github.mmolosay.thecolor.presentation.home.input.hex

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.presentation.home.input.ColorInputFieldUiData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ColorInputHexViewModel @Inject constructor() : ViewModel() {

    private val _uiDataFlow = MutableStateFlow(makeInitialViewModelData())
    val uiDataFlow = _uiDataFlow.asStateFlow()

    private fun processInput(input: String): String =
        input.take(MAX_SYMBOLS_IN_HEX_COLOR)

    private fun onInputChange(input: String) {
        _uiDataFlow.update {
            it.smartCopy(input = input)
        }
    }

    private fun onTrailingButtonClick() {
        _uiDataFlow.update {
            it.smartCopy(input = "")
        }
    }

    // seems like a better solution than "uiDataFlow = _uiDataFlow.map {..}"
    private fun ColorInputFieldUiData.ViewModelData.smartCopy(
        input: String,
    ) =
        copy(
            text = processInput(input),
            showTrailingButton = input.isNotEmpty(),
        )

    private fun makeInitialViewModelData() =
        ColorInputFieldUiData.ViewModelData(
            text = "",
            onTextChange = ::onInputChange,
            processText = ::processInput,
            showTrailingButton = false,
            onTrailingButtonClick = ::onTrailingButtonClick,
        )

    private companion object {
        const val MAX_SYMBOLS_IN_HEX_COLOR = 6
    }
}