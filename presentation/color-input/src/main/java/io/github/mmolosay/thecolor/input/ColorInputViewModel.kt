package io.github.mmolosay.thecolor.input

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.input.ColorInputMediator.ColorState
import io.github.mmolosay.thecolor.input.ColorInputUiData.ViewType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ColorInputViewModel.Factory::class)
class ColorInputViewModel @AssistedInject constructor(
    @Assisted viewData: ColorInputUiData.ViewData,
    private val mediator: ColorInputMediator,
) : ViewModel() {

    private val _uiDataFlow = MutableStateFlow(initialUiData(viewData))
    val uiDataFlow = _uiDataFlow.asStateFlow()

    // TODO: used to connect Compose-oriented ViewModel with old UI in View.
    //       Refactor once old UI is gone.
    val currentColorFlow: StateFlow<Color.Abstract?> =
        mediator.colorStateFlow
            .filterNotNull()
            .map { colorState ->
                when (colorState) {
                    is ColorState.Invalid -> null
                    is ColorState.Valid -> colorState.color
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null,
            )

    init {
        viewModelScope.launch {
            mediator.init()
        }
    }

    private fun onInputTypeChange(type: ViewType) {
        _uiDataFlow.update {
            it.copy(viewType = type)
        }
    }

    private fun initialUiData(viewData: ColorInputUiData.ViewData) =
        ColorInputUiData(
            viewType = ViewType.Hex,
            onInputTypeChange = ::onInputTypeChange,
            hexLabel = viewData.hexLabel,
            rgbLabel = viewData.rgbLabel,
        )

    @AssistedFactory
    interface Factory {
        fun create(viewData: ColorInputUiData.ViewData): ColorInputViewModel
    }
}