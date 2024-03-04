package io.github.mmolosay.thecolor.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.usecase.IsColorLightUseCase
import io.github.mmolosay.thecolor.presentation.ColorCenterCommandStore
import io.github.mmolosay.thecolor.presentation.ColorInputColorProvider
import io.github.mmolosay.thecolor.presentation.ColorToColorIntUseCase
import io.github.mmolosay.thecolor.presentation.Command
import io.github.mmolosay.thecolor.presentation.home.HomeData.CanProceed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val colorInputColorProvider: ColorInputColorProvider,
    private val colorCenterCommandStore: ColorCenterCommandStore,
    private val colorToColorInt: ColorToColorIntUseCase,
    private val isColorLight: IsColorLightUseCase,
//    @Named("defa") private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _dataFlow = MutableStateFlow(initialData())
    val dataFlow = _dataFlow.asStateFlow()

    init {
        collectColorInputColor()
    }

    private fun collectColorInputColor() =
        viewModelScope.launch {  // TODO: not main dispatcher?
            colorInputColorProvider.colorFlow.collect { color ->
                _dataFlow.update {
                    it.copy(
                        canProceed = CanProceed(color),
                        colorUsedToProceed = null,
                    )
                }
            }
        }

    private fun proceed() {
        val color = currentColor ?: return
        val command = Command.FetchData(color)
        viewModelScope.launch {  // TODO: not main dispatcher?
            colorCenterCommandStore.updateWith(command)
            _dataFlow.update {
                it.copy(colorUsedToProceed = ColorFromColorInput(color))
            }
        }
    }

    private fun initialData() =
        HomeData(
            canProceed = CanProceed(currentColor),
            colorUsedToProceed = currentColor?.let { ColorFromColorInput(it) },
        )

    private fun CanProceed(color: Color?) =
        when (color != null) {
            true -> CanProceed.Yes(action = ::proceed)
            false -> CanProceed.No
        }

    private fun ColorFromColorInput(color: Color) =
        HomeData.ColorFromColorInput(
            color = with(colorToColorInt) { color.toColorInt() },
            isDark = with(isColorLight) { color.isLight().not() },
        )

    private val currentColor: Color?
        get() = colorInputColorProvider.colorFlow.value
}