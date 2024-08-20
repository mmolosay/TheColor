package io.github.mmolosay.thecolor.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.usecase.IsColorLightUseCase
import io.github.mmolosay.thecolor.presentation.impl.ColorCenterCommand
import io.github.mmolosay.thecolor.presentation.impl.ColorCenterCommandStore
import io.github.mmolosay.thecolor.presentation.impl.ColorCenterEvent
import io.github.mmolosay.thecolor.presentation.impl.ColorCenterEventStore
import io.github.mmolosay.thecolor.presentation.impl.ColorRole
import io.github.mmolosay.thecolor.presentation.impl.ColorToColorIntUseCase
import io.github.mmolosay.thecolor.presentation.center.ColorCenterViewModel
import io.github.mmolosay.thecolor.presentation.home.HomeData.CanProceed
import io.github.mmolosay.thecolor.presentation.input.impl.ColorInputMediator
import io.github.mmolosay.thecolor.presentation.input.impl.ColorInputViewModel
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewViewModel
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputColorProvider
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputColorStore
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputEvent
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputEventStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    colorInputMediatorFactory: ColorInputMediator.Factory,
    colorInputViewModelFactory: ColorInputViewModel.Factory,
    colorPreviewViewModelFactory: ColorPreviewViewModel.Factory,
    colorCenterViewModelFactory: ColorCenterViewModel.Factory,
    getInitialModelsFactory: GetInitialModelsUseCase.Factory,
    private val colorInputColorStore: ColorInputColorStore,
    private val colorInputEventStore: ColorInputEventStore,
    private val colorCenterCommandStore: ColorCenterCommandStore,
    private val colorCenterEventStore: ColorCenterEventStore,
    private val createColorData: CreateColorDataUseCase,
    @Named("defaultDispatcher") private val defaultDispatcher: CoroutineDispatcher,
    @Named("uiDataUpdateDispatcher") private val uiDataUpdateDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val getInitialModels =
        getInitialModelsFactory.create(colorInputColorStore)

    private val _dataFlow = MutableStateFlow(HomeData(models = getInitialModels()))
    val dataFlow = _dataFlow.asStateFlow()

    private val _navEventFlow = MutableStateFlow<HomeNavEvent?>(null)
    val navEventFlow = _navEventFlow.asStateFlow()

    private val colorInputMediator: ColorInputMediator =
        colorInputMediatorFactory.create(
            colorInputColorStore = colorInputColorStore,
        )

    val colorInputViewModel: ColorInputViewModel =
        colorInputViewModelFactory.create(
            coroutineScope = viewModelScope,
            colorInputEventStore = colorInputEventStore,
            colorInputMediator = colorInputMediator,
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
            colorCenterEventStore = colorCenterEventStore,
        )

    init {
        collectColorsFromColorInput()
        collectEventsFromColorInput()
        collectEventsFromColorCenter()
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

    private fun collectEventsFromColorCenter() =
        viewModelScope.launch(defaultDispatcher) {
            colorCenterEventStore.eventFlow
                .collect(::onEventFromColorCenter)
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
            is ColorInputEvent.Submit ->
                proceed(colorRole = null) // standalone color
        }
    }

    private fun onEventFromColorCenter(event: ColorCenterEvent) {
        when (event) {
            is ColorCenterEvent.ColorSelected ->
                setColorAndProceed(
                    newColor = event.color,
                    colorRole = event.colorRole,
                )
        }
    }

    private fun proceed(
        colorRole: ColorRole?,
    ) {
        // TODO: add handling of case with no valid color in color input when color is
        //  submitted via IME keyboard action, so that user is notified
        val color = colorInputColorStore.colorFlow.value ?: return
        val command = ColorCenterCommand.FetchData(color, colorRole)
        viewModelScope.launch(defaultDispatcher) {
            colorCenterCommandStore.issue(command)
            _dataFlow.update {
                it.copy(colorUsedToProceed = createColorData(color))
            }
        }
    }

    private fun setColorAndProceed(
        newColor: Color,
        colorRole: ColorRole?,
    ) {
        viewModelScope.launch(defaultDispatcher) {
            // it's crucial to update color first, so that 'proceed()' obtains new color
            withContext(uiDataUpdateDispatcher) {
                colorInputMediator.send(color = newColor, from = null)
            }
            proceed(colorRole)
        }
    }

    private fun setGoToSettingsNavEvent() {
        _navEventFlow.value = NavEventGoToSettings()
    }

    private fun clearNavEvent() {
        _navEventFlow.value = null
    }

    /** Creates [HomeData] by combining passed [models] with ViewModel methods. */
    private fun HomeData(models: HomeData.Models) =
        HomeData(
            canProceed = CanProceed(canProceed = models.canProceed),
            colorUsedToProceed = models.colorUsedToProceed,
            goToSettings = ::setGoToSettingsNavEvent,
        )

    private fun CanProceed(canProceed: Boolean): CanProceed =
        when (canProceed) {
            true -> {
                val action: () -> Unit = {
                    // clicking "proceed" button takes color from color input,
                    // thus it's a standalone color (without a role)
                    proceed(colorRole = null)
                }
                CanProceed.Yes(action)
            }
            false -> CanProceed.No
        }

    private fun NavEventGoToSettings() =
        HomeNavEvent.GoToSettings(
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
        )
    }

    @AssistedFactory
    fun interface Factory {
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