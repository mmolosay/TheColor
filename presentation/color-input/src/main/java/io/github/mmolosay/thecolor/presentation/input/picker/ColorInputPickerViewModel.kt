package io.github.mmolosay.thecolor.presentation.input.picker

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.usecase.ColorConverter
import io.github.mmolosay.thecolor.presentation.common.viewmodel.SimpleViewModel
import io.github.mmolosay.thecolor.presentation.input.ColorInputMediator
import io.github.mmolosay.thecolor.presentation.input.colorOrNull
import io.github.mmolosay.thecolor.presentation.input.model.DataState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Named
import io.github.mmolosay.thecolor.domain.model.ColorInputType as DomainColorInputType

class ColorInputPickerViewModel @AssistedInject constructor(
    @Assisted coroutineScope: CoroutineScope,
    @Assisted private val mediator: ColorInputMediator,
    private val colorConverter: ColorConverter,
    @Named("defaultDispatcher") private val defaultDispatcher: CoroutineDispatcher,
) : SimpleViewModel(coroutineScope) {

    private val _dataStateFlow = MutableStateFlow<DataState<ColorInputPickerData>>(DataState.BeingInitialized)
    val dataStateFlow: StateFlow<DataState<ColorInputPickerData>> = _dataStateFlow.asStateFlow()

    init {
        coroutineScope.launch(defaultDispatcher) {
            mediator.colorStateFlow.collect { (colorState, source) ->
                // don't update text fields to avoid update loop if the color was set from this 'Color Input' type
//                 if (source == DomainColorInputType.VisualPicker) return@collect
                val color = colorState.colorOrNull()
                val data = ColorInputPickerData(
                    color = with(colorConverter) { color?.toHsv() },
                    onColorChanged = ::onColorChanged,
                )
                _dataStateFlow.value = DataState.Ready(data)
            }
        }
    }

    private fun onColorChanged(newColor: Color.Hsv) {
        mediator.send(color = newColor, from = DomainColorInputType.VisualPicker)
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            coroutineScope: CoroutineScope,
            mediator: ColorInputMediator,
        ): ColorInputPickerViewModel
    }
}