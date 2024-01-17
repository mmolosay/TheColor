package io.github.mmolosay.thecolor.presentation.home.input

import androidx.lifecycle.ViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.presentation.home.input.ColorInputUiData.InputType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel(assistedFactory = ColorInputViewModel.Factory::class)
class ColorInputViewModel @AssistedInject constructor(
    @Assisted viewData: ColorInputUiData.ViewData,
) : ViewModel() {

    private val _uiDataFlow = MutableStateFlow(initialUiData(viewData))
    val uiDataFlow = _uiDataFlow.asStateFlow()

    private fun onInputTypeSelect(type: InputType) {
        _uiDataFlow.update {
            it.copy(inputType = type)
        }
    }

    private fun initialUiData(viewData: ColorInputUiData.ViewData) =
        ColorInputUiData(
            inputType = InputType.Hex,
            onInputTypeSelect = ::onInputTypeSelect,
            hexLabel = viewData.hexLabel,
            rgbLabel = viewData.rgbLabel,
        )

    @AssistedFactory
    interface Factory {
        fun create(viewData: ColorInputUiData.ViewData): ColorInputViewModel
    }
}