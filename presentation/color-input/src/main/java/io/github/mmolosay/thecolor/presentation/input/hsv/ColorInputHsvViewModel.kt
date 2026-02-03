package io.github.mmolosay.thecolor.presentation.input.hsv

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.usecase.ColorConverter
import io.github.mmolosay.thecolor.presentation.common.viewmodel.SimpleViewModel
import io.github.mmolosay.thecolor.presentation.input.ColorInputMediator
import io.github.mmolosay.thecolor.presentation.input.model.DataState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Named
import io.github.mmolosay.thecolor.domain.model.ColorInputType as DomainColorInputType

/**
 * Handles presentation logic of the 'HEX Color Input' feature.
 *
 * Unlike typical `ViewModel`s, it doesn't derive from Google's [ViewModel][androidx.lifecycle.ViewModel],
 * thus cannot be instantiated using [ViewModelProvider][androidx.lifecycle.ViewModelProvider].
 *
 * Instead, it can be created within "simple" `ViewModel` or Google's `ViewModel`.
 */
class ColorInputHsvViewModel @AssistedInject constructor(
    @Assisted coroutineScope: CoroutineScope,
    @Assisted private val mediator: ColorInputMediator,
    private val colorConverter: ColorConverter,
    @Named("defaultDispatcher") private val defaultDispatcher: CoroutineDispatcher,
) : SimpleViewModel(coroutineScope) {

    private val _dataStateFlow = MutableStateFlow<DataState<ColorInputHsvData>>(DataState.BeingInitialized)
    val dataStateFlow: StateFlow<DataState<ColorInputHsvData>> = _dataStateFlow.asStateFlow()

    init {
        coroutineScope.launch(defaultDispatcher) {
            mediator.colorStateFlow.collect { (color, source) ->
                // don't update text fields to avoid update loop if the color was set from this 'Color Input' type
//                 if (source == DomainColorInputType.VisualPicker) return@collect
                val data = ColorInputHsvData(
                    color = with(colorConverter) { color?.toHsv() },
                    onColorChanged = ::onColorChanged,
                )
                _dataStateFlow.value = DataState.Ready(data)
            }
        }
    }

    private fun onColorChanged(newColor: Color.Hsv) {
        mediator.set(color = newColor, source = DomainColorInputType.Hsv)
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            coroutineScope: CoroutineScope,
            mediator: ColorInputMediator,
        ): ColorInputHsvViewModel
    }
}