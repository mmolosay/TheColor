package io.github.mmolosay.thecolor.presentation.home.input.hex

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.presentation.home.input.field.ColorInputFieldUiData
import io.github.mmolosay.thecolor.presentation.home.input.field.ColorInputFieldUiData.ViewData
import io.github.mmolosay.thecolor.presentation.home.input.field.ColorInputFieldViewModel
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
                initialValue = makeInitialUiData(),
            )

    private fun processInput(text: String): String =
        text
            .filter { it.isDigit() || it in 'A'..'F' }
            .take(MAX_SYMBOLS_IN_HEX_COLOR)

    private fun makeUiData(inputField: ColorInputFieldUiData) =
        ColorInputHexUiData(inputField)

    private fun makeInitialUiData() =
        makeUiData(colorInputFieldViewModel.uiDataFlow.value)

    private companion object {
        const val MAX_SYMBOLS_IN_HEX_COLOR = 6
    }

    @AssistedFactory
    interface Factory {
        fun create(viewData: ViewData): ColorInputHexViewModel
    }
}