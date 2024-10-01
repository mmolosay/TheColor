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
import kotlinx.coroutines.CoroutineScope
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

    private val colorCenterComponentsFlow = MutableStateFlow(ColorCenterComponents())
    private val colorCenterComponents: ColorCenterComponents
        get() = colorCenterComponentsFlow.value
    val colorCenterViewModel: ColorCenterViewModel
        get() = colorCenterComponents.colorCenterViewModel

    private var colorCenterSession: ColorCenterSession? = null

    init {
        collectColorsFromColorInput()
        collectEventsFromColorInput()
        collectColorCenterComponents()
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

    private fun collectColorCenterComponents() =
        viewModelScope.launch(defaultDispatcher) {
            colorCenterComponentsFlow.collect { components ->
                /*
                 * Subscribe to new dependencies once new Color Center is created.
                 * Launch collection coroutines from Color Center coroutine scope,
                 * so when ColorCenterViewModel is disposed and its coroutine scope is cancelled,
                 * so is the collection job on old Command/Event stores.
                */
                val coroutineScope = components.colorCenterCoroutineScope
                components.colorDetailsEventStore.collect(coroutineScope)
            }
        }

    private fun ColorDetailsEventStore.collect(coroutineScope: CoroutineScope) =
        coroutineScope.launch(defaultDispatcher) {
            eventFlow.collect(::onEventFromColorDetails)
        }

    private fun onColorFromColorInput(color: Color?) {
        val session = colorCenterSession
        if (session != null && color != null && with(session) { color.doesBelongToSession() }) {
            return // ignore re-emitted color or colors that are part of ongoing session
        }
        _dataFlow.update {
            it.copy(
                canProceed = CanProceed(colorFromColorInput = color),
                proceedResult = null, // 'proceed' wasn't invoked for new color yet
            )
        }
        onColorCenterSessionEnded()
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
            is ColorDetailsEvent.DataFetched -> {
                val session = requireNotNull(colorCenterSession)
                val details = event.domainDetails
                if (session.seed != details.color) return
                if (session.allowedColors != null) return
                session.allowedColors = setOf(
                    details.color,
                    details.exact.color,
                )
            }
            is ColorDetailsEvent.ColorSelected -> {
                viewModelScope.launch(defaultDispatcher) {
                    withContext(uiDataUpdateDispatcher) {
                        colorInputMediator.send(color = event.color, from = null)
                    }
                    proceed(
                        color = event.color,
                        colorRole = event.colorRole,
                        isNewColorCenterSession = false,
                    )
                }
            }
        }
    }

    private fun proceed(
        color: Color,
        colorRole: ColorRole?,
        isNewColorCenterSession: Boolean,
    ) {
        viewModelScope.launch(defaultDispatcher) {
            // recreate Color Center ViewModel (and its sub-feature ViewModels) to reset their states
            if (isNewColorCenterSession) {
                onColorCenterSessionStarted(color)
                recreateColorCenter()
            }
            // send to both features of Color Center explicitly
            run sendToColorDetails@{
                val command = ColorDetailsCommand.FetchData(color, colorRole)
                colorCenterComponents.colorDetailsCommandStore.issue(command)
            }
            run sendToColorScheme@{
                val command = ColorSchemeCommand.FetchData(color)
                colorCenterComponents.colorSchemeCommandStore.issue(command)
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
                isNewColorCenterSession = true,
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

    private fun initialData(): HomeData =
        HomeData(
            canProceed = CanProceed(),
            proceedResult = null, // 'proceed' action wasn't invoked yet
            goToSettings = ::setGoToSettingsNavEvent,
        )

    private fun CanProceed(): CanProceed {
        val color = colorInputColorStore.colorFlow.value
        return CanProceed(colorFromColorInput = color)
    }

    private fun CanProceed(colorFromColorInput: Color?): CanProceed {
        val hasColorInColorInput = (colorFromColorInput != null)
        return CanProceed(canProceed = hasColorInColorInput)
    }

    private fun CanProceed(canProceed: Boolean): CanProceed =
        when (canProceed) {
            true -> CanProceed.Yes(action = ::proceedAction)
            false -> CanProceed.No
        }

    private fun proceedAction() {
        // clicking "proceed" button takes color from Color Input,
        // thus it's a standalone color (without a role)
        val color = requireNotNull(colorInputColorStore.colorFlow.value)
        proceed(
            color = color,
            colorRole = null,
            isNewColorCenterSession = true, // color from Color Input, thus new session
        )
    }

    private fun NavEventGoToSettings() =
        HomeNavEvent.GoToSettings(
            onConsumed = ::clearNavEvent,
        )

    private fun ColorCenterComponents(): ColorCenterComponents {
        val colorCenterCoroutineScope = ViewModelCoroutineScope(parent = viewModelScope)
        val colorDetailsCommandStore = colorDetailsCommandStoreProvider.get()
        val colorDetailsEventStore = colorDetailsEventStoreProvider.get()
        val colorSchemeCommandStore = colorSchemeCommandStoreProvider.get()
        val colorCenterViewModel = colorCenterViewModelFactory.create(
            coroutineScope = colorCenterCoroutineScope,
            colorDetailsCommandProvider = colorDetailsCommandStore,
            colorDetailsEventStore = colorDetailsEventStore,
            colorSchemeCommandProvider = colorSchemeCommandStore,
        )
        return ColorCenterComponents(
            colorCenterViewModel = colorCenterViewModel,
            colorCenterCoroutineScope = colorCenterCoroutineScope,
            colorDetailsCommandStore = colorDetailsCommandStore,
            colorDetailsEventStore = colorDetailsEventStore,
            colorSchemeCommandStore = colorSchemeCommandStore,
        )
    }

    private fun recreateColorCenter() {
        colorCenterComponents.colorCenterViewModel.dispose()
        colorCenterComponentsFlow.value = ColorCenterComponents()
    }

    private fun onColorCenterSessionStarted(color: Color) {
        colorCenterSession = ColorCenterSession(color)
    }

    private fun onColorCenterSessionEnded() {
        colorCenterSession = null
    }
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

/**
 * A [ColorCenterViewModel] with the dependencies that it needs to be created via factory.
 * Once old [ColorCenterViewModel] is no longer needed, it will be disposed.
 * New components (and thus new ViewModel) will be created and used.
 */
private data class ColorCenterComponents(
    val colorCenterViewModel: ColorCenterViewModel,
    val colorCenterCoroutineScope: CoroutineScope,
    val colorDetailsCommandStore: ColorDetailsCommandStore,
    val colorDetailsEventStore: ColorDetailsEventStore,
    val colorSchemeCommandStore: ColorSchemeCommandStore,
)

/**
 * A data regarding current session of Color Center.
 * Session is tied to a [seed].
 * Session starts when color is submitted (proceeded with).
 * Session ends when color is cleared / changed via Color Input.
 * Any color can be checked whether it [doesBelongToSession].
 */
private class ColorCenterSession(
    val seed: Color,
) {
    var allowedColors: Set<Color>? = null

    fun Color.doesBelongToSession(): Boolean {
        if (this == seed) return true
        val allowedColors = allowedColors ?: return false
        return (this in allowedColors)
    }
}

/**
 * Determines whether the receiver session is already fully initialized.
 */
// TODO: kotlin contracts for parameter properties are not supported at the moment
private val ColorCenterSession.isInitialized: Boolean
    get() = (this.allowedColors != null)