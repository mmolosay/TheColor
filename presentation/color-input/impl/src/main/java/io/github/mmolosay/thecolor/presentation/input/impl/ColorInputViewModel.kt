package io.github.mmolosay.thecolor.presentation.input.impl

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.model.UserPreferences
import io.github.mmolosay.thecolor.domain.repository.UserPreferencesRepository
import io.github.mmolosay.thecolor.presentation.api.SimpleViewModel
import io.github.mmolosay.thecolor.presentation.api.ViewModelCoroutineScope
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputEventStore
import io.github.mmolosay.thecolor.presentation.input.impl.ColorInputData.ViewType
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
import io.github.mmolosay.thecolor.domain.model.UserPreferences as DomainUserPreferences

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

    private fun onInputTypeChange(type: ViewType) {
        _dataStateFlow.update { dataState ->
            val currentData = (dataState as? DataState.Ready)?.data ?: return@update dataState
            val newData = currentData.copy(
                selectedViewType = type,
            )
            DataState.Ready(newData)
        }
    }

    private suspend fun initialData(): ColorInputData {
        val preferredColorInputType = userPreferencesRepository.flowOfColorInputType().first()
        val preferredViewType = preferredColorInputType.toPresentation()
        // make list of all 'ViewType's with the preferred one being first
        val orderedViewTypes = run {
            val allViewTypes = ViewType.entries
            val allViewTypesWithoutPreferredOne = allViewTypes.filter { it != preferredViewType }
            listOf(preferredViewType) + allViewTypesWithoutPreferredOne
        }
        return ColorInputData(
            selectedViewType = preferredViewType,
            orderedViewTypes = orderedViewTypes,
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

// TODO: there are multiple enums that depict Color Input type. Unify in single domain enum?
private fun DomainUserPreferences.ColorInputType.toPresentation(): ViewType =
    when (this) {
        UserPreferences.ColorInputType.Hex -> ViewType.Hex
        UserPreferences.ColorInputType.Rgb -> ViewType.Rgb
    }