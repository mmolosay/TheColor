package io.github.mmolosay.thecolor.input

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.input.ColorInputUiData.ViewType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ColorInputViewModel.Factory::class)
class ColorInputViewModel @AssistedInject constructor(
    @Assisted viewData: ColorInputUiData.ViewData,
    private val mediator: ColorInputMediator,
) : ViewModel() {

    private val _uiDataFlow = MutableStateFlow(initialUiData(viewData))
    val uiDataFlow = _uiDataFlow.asStateFlow()

    init {
        viewModelScope.launch {
            mediator.init()
        }
    }

    private fun onInputTypeSelect(type: ViewType) {
        _uiDataFlow.update {
            it.copy(viewType = type)
        }
    }

    private fun initialUiData(viewData: ColorInputUiData.ViewData) =
        ColorInputUiData(
            viewType = ViewType.Hex,
            onInputTypeSelect = ::onInputTypeSelect,
            hexLabel = viewData.hexLabel,
            rgbLabel = viewData.rgbLabel,
        )

    @AssistedFactory
    interface Factory {
        fun create(viewData: ColorInputUiData.ViewData): ColorInputViewModel
    }
}