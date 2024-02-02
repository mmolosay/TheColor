package io.github.mmolosay.thecolor.presentation.input

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.presentation.input.ColorInputData.ViewType
import io.github.mmolosay.thecolor.presentation.input.ColorInputMediator.ColorState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ColorInputViewModel @Inject constructor(
    private val mediator: ColorInputMediator,
) : ViewModel() {

    private val _dataFlow = MutableStateFlow(initialData())
    val dataFlow = _dataFlow.asStateFlow()

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
        _dataFlow.update {
            it.copy(viewType = type)
        }
    }

    private fun initialData() =
        ColorInputData(
            viewType = ViewType.Hex,
            onInputTypeChange = ::onInputTypeChange,
        )
}