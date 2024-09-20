package io.github.mmolosay.thecolor.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.usecase.IsColorLightUseCase
import io.github.mmolosay.thecolor.presentation.api.ColorToColorIntUseCase
import io.github.mmolosay.thecolor.presentation.api.ViewModelCoroutineScope
import io.github.mmolosay.thecolor.presentation.center.ColorCenterViewModel
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsCommand
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsCommandStore
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsEvent
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsEventStore
import io.github.mmolosay.thecolor.presentation.details.ColorRole
import io.github.mmolosay.thecolor.presentation.home.HomeData.CanProceed
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputColorStore
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputEvent
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputEventStore
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputState
import io.github.mmolosay.thecolor.presentation.input.impl.ColorInputMediator
import io.github.mmolosay.thecolor.presentation.input.impl.ColorInputViewModel
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewViewModel
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeCommand
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeCommandStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
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
    private val colorCenterViewModelFactory: ColorCenterViewModel.Factory,
    private val colorInputColorStore: ColorInputColorStore,
    private val colorInputEventStore: ColorInputEventStore,
    private val colorDetailsCommandStoreProvider: Provider<ColorDetailsCommandStore>,
    private val colorDetailsEventStoreProvider: Provider<ColorDetailsEventStore>,
    private val colorSchemeCommandStoreProvider: Provider<ColorSchemeCommandStore>,
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
            coroutineScope = ViewModelCoroutineScope(parent = viewModelScope),
            colorInputEventStore = colorInputEventStore,
            colorInputMediator = colorInputMediator,
        )

    val colorPreviewViewModel: ColorPreviewViewModel =
        colorPreviewViewModelFactory.create(
            coroutineScope = ViewModelCoroutineScope(parent = viewModelScope),
            colorInputColorProvider = colorInputColorStore,
        )

    private var colorDetailsCommandStore = colorDetailsCommandStoreProvider.get()
    private var colorDetailsEventStore = colorDetailsEventStoreProvider.get()
    private var colorDetailsEventCollectionJob: Job? = null
    private var colorSchemeCommandStore = colorSchemeCommandStoreProvider.get()
    var colorCenterViewModel: ColorCenterViewModel = newColorCenterViewModel()

    init {
        collectColorsFromColorInput()
        collectEventsFromColorInput()
        collectEventsFromColorDetails()
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

    private fun collectEventsFromColorDetails() =
        viewModelScope.launch(defaultDispatcher) {
            colorDetailsEventStore.eventFlow
                .collect(::onEventFromColorDetails)
        }.also {
            colorDetailsEventCollectionJob?.cancel()
            colorDetailsEventCollectionJob = it
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

    private fun onEventFromColorDetails(event: ColorDetailsEvent) {
        when (event) {
            is ColorDetailsEvent.ColorSelected -> {
                viewModelScope.launch(defaultDispatcher) {
                    withContext(uiDataUpdateDispatcher) {
                        colorInputMediator.send(color = event.color, from = null)
                    }
                    proceed(
                        color = event.color,
                        colorRole = event.colorRole,
                        shouldRecreateColorCenter = false,
                    )
                }
            }
        }
    }

    private fun proceed(
        color: Color,
        colorRole: ColorRole?,
        shouldRecreateColorCenter: Boolean,
    ) {
        viewModelScope.launch(defaultDispatcher) {
            // recreate Color Center ViewModel (and its sub-feature ViewModels) to reset their states
            if (shouldRecreateColorCenter) {
                recreateColorCenter()
            }
            // send to both features of Color Center explicitly
            run sendToColorDetails@{
                val command = ColorDetailsCommand.FetchData(color, colorRole)
                colorDetailsCommandStore.issue(command)
            }
            run sendToColorScheme@{
                val command = ColorSchemeCommand.FetchData(color)
                colorSchemeCommandStore.issue(command)
            }
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
            proceed(
                color = colorInputState.color,
                colorRole = null,
                shouldRecreateColorCenter = true,
            )
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

    private fun newColorCenterViewModel() =
        colorCenterViewModelFactory.create(
            coroutineScope = ViewModelCoroutineScope(parent = viewModelScope),
            colorDetailsCommandProvider = colorDetailsCommandStore,
            colorDetailsEventStore = colorDetailsEventStore,
            colorSchemeCommandProvider = colorSchemeCommandStore,
        )

    private fun recreateColorCenter() {
        colorCenterViewModel.dispose()
        colorDetailsCommandStore = colorDetailsCommandStoreProvider.get()
        colorDetailsEventStore = colorDetailsEventStoreProvider.get()
        collectEventsFromColorDetails()
        colorSchemeCommandStore = colorSchemeCommandStoreProvider.get()
        colorCenterViewModel = newColorCenterViewModel()
    }

    private fun initialData(): HomeData =
        HomeData(
            canProceed = CanProceed(),
            proceedResult = null, // 'proceed' action wasn't invoked yet
            goToSettings = ::setGoToSettingsNavEvent,
        )

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
                    val color = requireNotNull(colorInputColorStore.colorFlow.value)
                    proceed(
                        color = color,
                        colorRole = null,
                        shouldRecreateColorCenter = true,
                    )
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