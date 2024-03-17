package io.github.mmolosay.thecolor.presentation.input

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.presentation.input.ColorInputData.ViewType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ColorInputViewModel @Inject constructor(
    private val mediator: ColorInputMediator,
) : ViewModel() {

    private val _dataFlow = MutableStateFlow(initialData())
    val dataFlow = _dataFlow.asStateFlow()

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