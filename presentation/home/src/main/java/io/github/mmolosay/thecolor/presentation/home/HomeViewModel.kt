package io.github.mmolosay.thecolor.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.usecase.IsColorLightUseCase
import io.github.mmolosay.thecolor.presentation.ColorCenterCommand
import io.github.mmolosay.thecolor.presentation.ColorCenterCommandStore
import io.github.mmolosay.thecolor.presentation.ColorInputColorProvider
import io.github.mmolosay.thecolor.presentation.ColorInputColorStore
import io.github.mmolosay.thecolor.presentation.ColorInputEvent
import io.github.mmolosay.thecolor.presentation.ColorInputEventStore
import io.github.mmolosay.thecolor.presentation.ColorToColorIntUseCase
import io.github.mmolosay.thecolor.presentation.center.ColorCenterViewModel
import io.github.mmolosay.thecolor.presentation.home.HomeData.CanProceed
import io.github.mmolosay.thecolor.presentation.input.ColorInputViewModel
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * An Android-aware [ViewModel] for "Home" View.
 * Composed of sub-feature ViewModels of nested Views.
 *
 * It creates objects that are shared between sub-feature ViewModels via assisted injection and
 * factories.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    colorInputViewModelFactory: ColorInputViewModel.Factory,
    colorPreviewViewModelFactory: ColorPreviewViewModel.Factory,
    colorCenterViewModelFactory: ColorCenterViewModel.Factory,
    getInitialModelsFactory: GetInitialModelsUseCase.Factory,
    private val colorInputColorStore: ColorInputColorStore,
    private val colorInputEventStore: ColorInputEventStore,
    private val colorCenterCommandStore: ColorCenterCommandStore,
    private val createColorData: CreateColorDataUseCase,
    @Named("defaultDispatcher") private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val getInitialModels =
        getInitialModelsFactory.create(colorInputColorStore)

    private val _dataFlow = MutableStateFlow(HomeData(models = getInitialModels()))
    val dataFlow = _dataFlow.asStateFlow()

    val colorInputViewModel: ColorInputViewModel =
        colorInputViewModelFactory.create(
            coroutineScope = viewModelScope,
            colorInputColorStore = colorInputColorStore,
            colorInputEventStore = colorInputEventStore,
        )

    val colorPreviewViewModel: ColorPreviewViewModel =
        colorPreviewViewModelFactory.create(
            coroutineScope = viewModelScope,
            colorInputColorProvider = colorInputColorStore,
        )

    val colorCenterViewModel: ColorCenterViewModel =
        colorCenterViewModelFactory.create(
            coroutineScope = viewModelScope,
            colorCenterCommandProvider = colorCenterCommandStore,
        )

    init {
        collectColorsFromColorInput()
        collectEventsFromColorInput()
    }

    private fun collectColorsFromColorInput() =
        viewModelScope.launch(defaultDispatcher) {
            colorInputColorStore.colorFlow
                .drop(1) // replayed value
                .collect(::onColorFromColorInput)
        }

    private fun collectEventsFromColorInput() =
        viewModelScope.launch(defaultDispatcher) {
            colorInputEventStore.eventFlow
                .collect(::onEventFromColorInput)
        }

    private fun onColorFromColorInput(color: Color?) {
        _dataFlow.update {
            it.copy(
                canProceed = CanProceed(canProceed = (color != null)),
                colorUsedToProceed = null, // 'proceed' action wasn't invoked yet
            )
        }
    }

    private fun onEventFromColorInput(event: ColorInputEvent) {
        when (event) {
            is ColorInputEvent.Submit -> proceed()
        }
    }

    private fun proceed() {
        // TODO: add handling of case with no valid color in color input, so that user is notified
        val color = colorInputColorStore.colorFlow.value ?: return
        val command = ColorCenterCommand.FetchData(color)
        viewModelScope.launch(defaultDispatcher) {
            colorCenterCommandStore.issue(command)
            _dataFlow.update {
                it.copy(colorUsedToProceed = createColorData(color))
            }
        }
    }

    private fun setGoToSettingsNavEvent() {
        _dataFlow.update {
            it.copy(navEvent = NavEventGoToSettings())
        }
    }

    private fun clearNavEvent() {
        _dataFlow.update {
            it.copy(navEvent = null)
        }
    }

    /** Creates [HomeData] by combining passed [models] with ViewModel methods. */
    private fun HomeData(models: HomeData.Models) =
        HomeData(
            canProceed = CanProceed(canProceed = models.canProceed),
            colorUsedToProceed = models.colorUsedToProceed,
            goToSettings = ::setGoToSettingsNavEvent,
            navEvent = when (models.navEvent) {
                is HomeData.Models.NavEvent.GoToSettings -> NavEventGoToSettings()
                null -> null
            },
        )

    private fun CanProceed(canProceed: Boolean): CanProceed =
        if (canProceed) CanProceed.Yes(action = ::proceed) else CanProceed.No

    private fun NavEventGoToSettings() =
        HomeData.NavEvent.GoToSettings(
            onConsumed = ::clearNavEvent,
        )
}

/**
 * Exists to make unit testing easier.
 * Replaces a long chain of actions that set [HomeViewModel.dataFlow] in "given" part
 * of the test to required value.
 */
class GetInitialModelsUseCase @AssistedInject constructor(
    @Assisted private val colorInputColorProvider: ColorInputColorProvider,
) {

    operator fun invoke(): HomeData.Models {
        val color = colorInputColorProvider.colorFlow.value
        return HomeData.Models(
            canProceed = (color != null),
            colorUsedToProceed = null, // 'proceed' action wasn't invoked yet
            navEvent = null,
        )
    }

    @AssistedFactory
    interface Factory {
        fun create(
            colorInputColorProvider: ColorInputColorProvider,
        ): GetInitialModelsUseCase
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