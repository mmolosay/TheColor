package io.github.mmolosay.thecolor.presentation.input.impl

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.presentation.api.SimpleViewModel
import io.github.mmolosay.thecolor.presentation.api.ViewModelCoroutineScope
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputEventStore
import io.github.mmolosay.thecolor.presentation.input.impl.ColorInputData.ViewType
import io.github.mmolosay.thecolor.presentation.input.impl.hex.ColorInputHexViewModel
import io.github.mmolosay.thecolor.presentation.input.impl.rgb.ColorInputRgbViewModel
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
    @Assisted eventStore: ColorInputEventStore,
    @Assisted mediator: ColorInputMediator,
    hexViewModelFactory: ColorInputHexViewModel.Factory,
    rgbViewModelFactory: ColorInputRgbViewModel.Factory,
) : SimpleViewModel(coroutineScope) {

    private val _dataFlow = MutableStateFlow(initialData())
    val dataFlow = _dataFlow.asStateFlow()

    val hexViewModel: ColorInputHexViewModel by lazy {
        hexViewModelFactory.create(
            coroutineScope = ViewModelCoroutineScope(parent = coroutineScope),
            mediator = mediator,
            eventStore = eventStore,
        )
    }

    val rgbViewModel: ColorInputRgbViewModel by lazy {
        rgbViewModelFactory.create(
            coroutineScope = ViewModelCoroutineScope(parent = coroutineScope),
            mediator = mediator,
            eventStore = eventStore,
        )
    }

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

    override fun dispose() {
        super.dispose()
        hexViewModel.dispose()
        rgbViewModel.dispose()
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            coroutineScope: CoroutineScope,
            colorInputEventStore: ColorInputEventStore,
            colorInputMediator: ColorInputMediator,
        ): ColorInputViewModel
    }
}