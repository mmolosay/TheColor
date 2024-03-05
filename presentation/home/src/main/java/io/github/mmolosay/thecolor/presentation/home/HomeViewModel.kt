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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@HiltViewModel
class HomeViewModel @Inject constructor(
    getInitialModels: GetInitialModelsUseCase,
    private val colorInputColorProvider: ColorInputColorProvider,
    private val colorCenterCommandStore: ColorCenterCommandStore,
    private val createColorData: CreateColorDataUseCase,
    @Named("defaultDispatcher") private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _dataFlow = MutableStateFlow(HomeData(getInitialModels()))
    val dataFlow = _dataFlow.asStateFlow()

    init {
        collectColorFromColorInput()
    }

    private fun collectColorFromColorInput() =
        viewModelScope.launch(defaultDispatcher) {
            colorInputColorProvider.colorFlow.collect(::onColorFromColorInput)
        }

    private fun onColorFromColorInput(color: Color?) {
        val models = HomeData.Models(
            canProceed = (color != null),
            colorUsedToProceed = null, // 'proceed' action wasn't invoked yet
        )
        _dataFlow updateWith models
    }

    private fun proceed() {
        val color = colorInputColorProvider.colorFlow.value ?: return
        val command = Command.FetchData(color)
        viewModelScope.launch(defaultDispatcher) {
            colorCenterCommandStore.issue(command)
            _dataFlow.update {
                it.copy(colorUsedToProceed = createColorData(color))
            }
        }
    }

    /** Creates [HomeData] by combining passed [models] with ViewModel methods. */
    private fun HomeData(models: HomeData.Models) =
        HomeData(
            canProceed = if (models.canProceed) CanProceed.Yes(action = ::proceed) else CanProceed.No,
            colorUsedToProceed = models.colorUsedToProceed,
        )

    private infix fun MutableStateFlow<HomeData>.updateWith(models: HomeData.Models) {
        this.value = HomeData(models)
    }
}

/**
 * Exists to make unit testing easier.
 * Replaces a long chain of actions that set [HomeViewModel.dataFlow] in "given" part
 * of the test to required value.
 */
@Singleton
class GetInitialModelsUseCase @Inject constructor(
    private val colorInputColorProvider: ColorInputColorProvider,
) {

    operator fun invoke(): HomeData.Models {
        val color = colorInputColorProvider.colorFlow.value
        return HomeData.Models(
            canProceed = (color != null),
            colorUsedToProceed = null, // 'proceed' action wasn't invoked yet
        )
    }
}

@Singleton
class CreateColorDataUseCase @Inject constructor(
    private val colorToColorInt: ColorToColorIntUseCase,
    private val isColorLight: IsColorLightUseCase,
) {

    operator fun invoke(color: Color) =
        HomeData.ColorData(
            color = with(colorToColorInt) { color.toColorInt() },
            isDark = with(isColorLight) { color.isLight().not() },
        )
}