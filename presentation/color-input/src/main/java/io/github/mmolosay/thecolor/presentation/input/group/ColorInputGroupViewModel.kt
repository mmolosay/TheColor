package io.github.mmolosay.thecolor.presentation.input.group

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.presentation.common.viewmodel.SimpleViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import io.github.mmolosay.thecolor.domain.color.ColorInputType as DomainColorInputType

/**
 * Handles presentation logic of the 'Color Input Group' feature.
 *
 * Unlike typical `ViewModel`s, it doesn't derive from Google's [ViewModel][androidx.lifecycle.ViewModel],
 * thus cannot be instantiated using [ViewModelProvider][androidx.lifecycle.ViewModelProvider].
 *
 * Instead, it can be created within "simple" `ViewModel` or Google's `ViewModel`.
 */
class ColorInputGroupViewModel @AssistedInject constructor(
    @Assisted coroutineScope: CoroutineScope,
    @Assisted initialData: ColorInputGroupData,
) : SimpleViewModel(coroutineScope) {

    private val _dataFlow = MutableStateFlow(initialData)
    val dataFlow: StateFlow<ColorInputGroupData> = _dataFlow.asStateFlow()

    fun execute(action: ColorInputGroupAction): Job? =
        when (action) {
            is ColorInputGroupAction.ChangeInputType -> {
                changeInputType(action.type)
                null
            }
        }

    private fun changeInputType(type: DomainColorInputType) {
        _dataFlow.update {
            it.copy(selectedInputType = type)
        }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            coroutineScope: CoroutineScope,
            initialData: ColorInputGroupData,
        ): ColorInputGroupViewModel
    }
}