package io.github.mmolosay.thecolor.presentation.input.impl

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.repository.UserPreferencesRepository
import io.github.mmolosay.thecolor.presentation.api.SimpleViewModel
import io.github.mmolosay.thecolor.presentation.api.ViewModelCoroutineScope
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputEventStore
import io.github.mmolosay.thecolor.presentation.input.impl.hex.ColorInputHexViewModel
import io.github.mmolosay.thecolor.presentation.input.impl.rgb.ColorInputRgbViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Named
import io.github.mmolosay.thecolor.domain.model.ColorInputType as DomainColorInputType

/**
 * Handles presentation logic of the 'Color Input' feature.
 *
 * Unlike typical `ViewModel`s, it doesn't derive from Google's [ViewModel][androidx.lifecycle.ViewModel],
 * thus cannot be instantiated using [ViewModelProvider][androidx.lifecycle.ViewModelProvider].
 *
 * Instead, it can be created within "simple" `ViewModel` or Google's `ViewModel`.
 */
class ColorInputViewModel @AssistedInject constructor(
    @Assisted coroutineScope: CoroutineScope,
    @Assisted eventStore: ColorInputEventStore,
    @Assisted mediator: ColorInputMediator,
    hexViewModelFactory: ColorInputHexViewModel.Factory,
    rgbViewModelFactory: ColorInputRgbViewModel.Factory,
    private val userPreferencesRepository: UserPreferencesRepository,
    @Named("defaultDispatcher") private val defaultDispatcher: CoroutineDispatcher,
) : SimpleViewModel(coroutineScope) {

    private val _dataStateFlow = MutableStateFlow<DataState>(DataState.Loading)
    val dataStateFlow = _dataStateFlow.asStateFlow()

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
        coroutineScope.launch(defaultDispatcher) {
            _dataStateFlow.value = DataState.Ready(data = initialData())
        }
    }

    private fun onInputTypeChange(type: DomainColorInputType) {
        _dataStateFlow.update { dataState ->
            val currentData = (dataState as? DataState.Ready)?.data ?: return@update dataState
            val newData = currentData.copy(
                selectedInputType = type,
            )
            DataState.Ready(newData)
        }
    }

    private suspend fun initialData(): ColorInputData {
        val preferredInputType = userPreferencesRepository.flowOfColorInputType().first()
        // make list of all input types with the preferred one being first
        val orderedViewTypes = run {
            val allInputTypes = DomainColorInputType.entries
            val allInputTypesWithoutPreferredOne = allInputTypes.filter { it != preferredInputType }
            listOf(preferredInputType) + allInputTypesWithoutPreferredOne
        }
        return ColorInputData(
            selectedInputType = preferredInputType,
            orderedInputTypes = orderedViewTypes,
            onInputTypeChange = ::onInputTypeChange,
        )
    }


    override fun dispose() {
        super.dispose()
        hexViewModel.dispose()
        rgbViewModel.dispose()
    }

    interface DataState {
        data object Loading : DataState
        data class Ready(val data: ColorInputData) : DataState
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