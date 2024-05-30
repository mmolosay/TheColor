package io.github.mmolosay.thecolor.presentation.input

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.presentation.ColorInputColorStore
import io.github.mmolosay.thecolor.presentation.ColorInputEventStore
import io.github.mmolosay.thecolor.presentation.input.ColorInputData.ViewType
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexViewModel
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Not a ViewModel-ViewModel in terms of Android development.
 * It doesn't derive from [androidx.lifecycle.ViewModel], so should only be used in "real" ViewModels
 * which do derive from Android-aware implementation.
 */
class ColorInputViewModel @AssistedInject constructor(
    @Assisted coroutineScope: CoroutineScope,
    @Assisted colorInputColorStore: ColorInputColorStore,
    @Assisted colorInputEventStore: ColorInputEventStore,
    hexViewModelFactory: ColorInputHexViewModel.Factory,
    rgbViewModelFactory: ColorInputRgbViewModel.Factory,
    mediatorFactory: ColorInputMediator.Factory,
) {

    private val _dataFlow = MutableStateFlow(initialData())
    val dataFlow = _dataFlow.asStateFlow()

    private val mediator: ColorInputMediator =
        mediatorFactory.create(
            colorInputColorStore = colorInputColorStore,
        )

    val hexViewModel: ColorInputHexViewModel =
        hexViewModelFactory.create(
            coroutineScope = coroutineScope,
            mediator = mediator,
            eventStore = colorInputEventStore,
        )

    val rgbViewModel: ColorInputRgbViewModel =
        rgbViewModelFactory.create(
            coroutineScope = coroutineScope,
            mediator = mediator,
            eventStore = colorInputEventStore,
        )

    init {
        coroutineScope.launch {
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

    @AssistedFactory
    interface Factory {
        fun create(
            coroutineScope: CoroutineScope,
            colorInputColorStore: ColorInputColorStore,
            colorInputEventStore: ColorInputEventStore,
        ): ColorInputViewModel
    }
}