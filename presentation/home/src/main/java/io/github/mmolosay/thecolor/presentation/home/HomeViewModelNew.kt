package io.github.mmolosay.thecolor.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.presentation.ColorCenterCommandStore
import io.github.mmolosay.thecolor.presentation.ColorInputColorProvider
import io.github.mmolosay.thecolor.presentation.ColorInputColorStore
import io.github.mmolosay.thecolor.presentation.Command
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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

    // TODO: make a part of exposed data
    fun proceed() {
        val color = colorInputColorProvider.colorFlow.value ?: return
        val command = Command.FetchData(color)
        viewModelScope.launch {
            colorCenterCommandStore.updateWith(command)
        }
    }
}