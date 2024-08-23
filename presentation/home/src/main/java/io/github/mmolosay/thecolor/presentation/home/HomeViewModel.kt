package io.github.mmolosay.thecolor.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.usecase.IsColorLightUseCase
import io.github.mmolosay.thecolor.presentation.api.ColorCenterCommand
import io.github.mmolosay.thecolor.presentation.api.ColorCenterCommandStore
import io.github.mmolosay.thecolor.presentation.api.ColorCenterEvent
import io.github.mmolosay.thecolor.presentation.api.ColorCenterEventStore
import io.github.mmolosay.thecolor.presentation.api.ColorRole
import io.github.mmolosay.thecolor.presentation.api.ColorToColorIntUseCase
import io.github.mmolosay.thecolor.presentation.center.ColorCenterViewModel
import io.github.mmolosay.thecolor.presentation.home.HomeData.CanProceed
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputColorStore
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputEvent
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputEventStore
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputState
import io.github.mmolosay.thecolor.presentation.input.impl.ColorInputMediator
import io.github.mmolosay.thecolor.presentation.input.impl.ColorInputViewModel
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewViewModel
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
    private val colorInputColorStore: ColorInputColorStore,
    private val colorInputEventStore: ColorInputEventStore,
    private val colorCenterCommandStore: ColorCenterCommandStore,
    private val colorCenterEventStore: ColorCenterEventStore,
    private val createColorData: CreateColorDataUseCase,
    @Named("defaultDispatcher") private val defaultDispatcher: CoroutineDispatcher,
    @Named("uiDataUpdateDispatcher") private val uiDataUpdateDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _dataFlow = MutableStateFlow(initialData())
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
                proceedResult = null, // 'proceed' wasn't invoked for new color yet
            )
        }
    }

    private fun onEventFromColorInput(event: ColorInputEvent) {
        when (event) {
            is ColorInputEvent.Submit -> {
                val hasProceeded = onColorInputSubmitted(event.colorInputState)
                event.onConsumed(wasAccepted = hasProceeded)
            }
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
        val color = colorInputColorStore.colorFlow.value ?: return
        proceed(color, colorRole)
    }

    private fun proceed(
        color: Color,
        colorRole: ColorRole?,
    ) {
        val command = ColorCenterCommand.FetchData(color, colorRole)
        viewModelScope.launch(defaultDispatcher) {
            colorCenterCommandStore.issue(command)
            val colorData = createColorData(color)
            val proceedResult = HomeData.ProceedResult.Success(
                colorData = colorData,
            )
            _dataFlow.update {
                it.copy(proceedResult = proceedResult)
            }
        }
    }

    private fun onColorInputSubmitted(
        colorInputState: ColorInputState,
    ): Boolean {
        if (colorInputState is ColorInputState.Valid) {
            proceed(colorInputState.color, null)
            return true
        } else {
            _dataFlow.update {
                val result = HomeData.ProceedResult.InvalidSubmittedColor(
                    discard = ::clearProceedResult,
                )
                it.copy(proceedResult = result)
            }
            return false
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

    private fun clearProceedResult() {
        _dataFlow.update {
            it.copy(proceedResult = null)
        }
    }

    private fun clearNavEvent() {
        _navEventFlow.value = null
    }

    private fun initialData(): HomeData {
        return HomeData(
            canProceed = CanProceed(),
            proceedResult = null, // 'proceed' action wasn't invoked yet
            goToSettings = ::setGoToSettingsNavEvent,
        )
    }

    private fun CanProceed(): CanProceed {
        val color = colorInputColorStore.colorFlow.value
        val hasColorInColorInput = (color != null)
        return CanProceed(canProceed = hasColorInColorInput)
    }

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

/** Creates instance of [HomeData.ProceedResult.Success.ColorData]. */
@Singleton
class CreateColorDataUseCase @Inject constructor(
    private val colorToColorInt: ColorToColorIntUseCase,
    private val isColorLight: IsColorLightUseCase,
) {

    operator fun invoke(color: Color) =
        HomeData.ProceedResult.Success.ColorData(
            color = with(colorToColorInt) { color.toColorInt() },
            isDark = with(isColorLight) { color.isLight().not() },
        )
}