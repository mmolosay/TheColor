package io.github.mmolosay.thecolor.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.presentation.ColorCenterCommandStore
import io.github.mmolosay.thecolor.presentation.ColorInputColorProvider
import io.github.mmolosay.thecolor.presentation.Command
import io.github.mmolosay.thecolor.presentation.home.HomeData.CanProceed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// TODO: remove "New" suffix once the old ViewModel is gone
@HiltViewModel
class HomeViewModelNew @Inject constructor(
    private val colorInputColorProvider: ColorInputColorProvider,
    private val colorCenterCommandStore: ColorCenterCommandStore,
) : ViewModel() {

    val proceedActionAvailabilityFlow: StateFlow<Boolean> =
        colorInputColorProvider.colorFlow
            .map { color -> color != null }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = false,
            )

    private val _dataFlow = MutableStateFlow(initialData())
    val dataFlow = _dataFlow.asStateFlow()

    init {
        collectColorInputColor()
    }

    private fun collectColorInputColor() =
        viewModelScope.launch {  // TODO: not main dispatcher?
            colorInputColorProvider.colorFlow.collect { color ->
                _dataFlow.update {
                    it.copy(canProceed = CanProceed(color))
                }
            }
        }

    // TODO: make a part of exposed data
    fun proceed() {
        val color = currentColor ?: return
        val command = Command.FetchData(color)
        viewModelScope.launch {  // TODO: not main dispatcher?
            colorCenterCommandStore.updateWith(command)
        }
    }

    private fun initialData() =
        HomeData(
            canProceed = CanProceed(currentColor),
        )

    private fun CanProceed(color: Color?) =
        when (color != null) {
            true -> CanProceed.Yes(action = ::proceed)
            false -> CanProceed.No
        }

    private val currentColor: Color?
        get() = colorInputColorProvider.colorFlow.value
}