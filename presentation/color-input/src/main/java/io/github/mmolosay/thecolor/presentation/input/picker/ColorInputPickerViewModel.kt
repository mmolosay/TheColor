package io.github.mmolosay.thecolor.presentation.input.picker

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.presentation.common.viewmodel.SimpleViewModel
import io.github.mmolosay.thecolor.presentation.input.ColorInputMediator
import kotlinx.coroutines.CoroutineScope

class ColorInputPickerViewModel @AssistedInject constructor(
    @Assisted coroutineScope: CoroutineScope,
    @Assisted private val mediator: ColorInputMediator,
) : SimpleViewModel(coroutineScope) {

    @AssistedFactory
    fun interface Factory {
        fun create(
            coroutineScope: CoroutineScope,
            mediator: ColorInputMediator,
        ): ColorInputPickerViewModel
    }
}