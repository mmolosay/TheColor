package io.github.mmolosay.thecolor.presentation.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.repository.LastSearchedColorRepository
import io.github.mmolosay.thecolor.domain.repository.UserPreferencesRepository
import io.github.mmolosay.thecolor.domain.usecase.IsColorLightUseCase
import io.github.mmolosay.thecolor.presentation.api.ColorToColorIntUseCase
import io.github.mmolosay.thecolor.presentation.api.ViewModelCoroutineScope
import io.github.mmolosay.thecolor.presentation.center.ColorCenterViewModel
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsCommand
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsCommandStore
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsEvent
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsEventStore
import io.github.mmolosay.thecolor.presentation.details.ColorRole
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeData.CanProceed
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton

/**
 * A [ViewModel] for 'Home' View.
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
    private val colorCenterSessionBuilder: ColorCenterSessionBuilder,
    private val doesColorBelongToSession: DoesColorBelongToSessionUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val lastSearchedColorRepository: LastSearchedColorRepository,
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

    private val colorCenterComponentsFlow = MutableStateFlow<ColorCenterComponents?>(null)
    val colorCenterViewModelFlow: StateFlow<ColorCenterViewModel?> =
        colorCenterComponentsFlow
            .map { it?.colorCenterViewModel }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null,
            )

    private var colorCenterSession: ColorCenterSession? = null
    private var dataFetchedEventProcessor: DataFetchedEventProcessor? = initialDataFetchedEventProcessor()

    init {
        collectColorsFromColorInput()
        collectEventsFromColorInput()
        collectColorCenterComponents()
        proceedWithLastSearchedColor()
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
            colorCenterComponentsFlow
                .filterNotNull()
                .collect { components ->
                    /*
                     * Subscribe to new dependencies once new Color Center is created.
                     * Launch collection coroutines from Color Center coroutine scope,
                     * so when ColorCenterViewModel is disposed of and its coroutine scope is cancelled,
                     * so is the collection job on old instances of Command/Event stores.
                    */
                    val coroutineScope = components.colorCenterCoroutineScope
                    components.colorDetailsEventStore.collect(coroutineScope)
                }
        }

    // TODO: add unit tests
    private fun proceedWithLastSearchedColor() {
        viewModelScope.launch(defaultDispatcher) {
            val resumeFromLastSearchedColorOnStartup = userPreferencesRepository
                .flowOfResumeFromLastSearchedColorOnStartup()
                .first()
            val enabled = resumeFromLastSearchedColorOnStartup.enabled
            if (!enabled) return@launch
            val lastSearchedColor =
                lastSearchedColorRepository.getLastSearchedColor() ?: return@launch
            updateColorInColorInput(color = lastSearchedColor)
            proceed(
                color = lastSearchedColor,
                colorRole = null,
                isNewColorCenterSession = true,
            )
        }
    }

    private fun ColorDetailsEventStore.collect(coroutineScope: CoroutineScope) =
        coroutineScope.launch(defaultDispatcher) {
            eventFlow.collect(::onEventFromColorDetails)
        }

    private fun onColorFromColorInput(color: Color?) {
        val session = colorCenterSession
        if (session != null && color != null &&
            with(doesColorBelongToSession) { color doesBelongTo session }
        ) {
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
                dataFetchedEventProcessor?.run { event.process() }
            }
            is ColorDetailsEvent.ColorSelected -> {
                viewModelScope.launch(defaultDispatcher) {
                    updateColorInColorInput(color = event.color)
                    proceed(
                        color = event.color,
                        colorRole = event.colorRole,
                        isNewColorCenterSession = false,
                    )
                }
            }
        }
    }

    /** Variation that takes current color of Color Input. */
    private fun proceed() {
        val color = requireNotNull(colorInputColorStore.colorFlow.value)
        proceed(
            color = color,
            colorRole = null, // standalone color (without a role)
            isNewColorCenterSession = true, // color from Color Input, thus new session
        )
    }

    private fun proceed(
        color: Color,
        colorRole: ColorRole?,
        isNewColorCenterSession: Boolean,
    ) {
        viewModelScope.launch(defaultDispatcher) {
            if (isNewColorCenterSession) {
                onColorCenterSessionStarted(color)
            }
            val colorCenterComponents = requireNotNull(colorCenterComponentsFlow.value)
            // send to both features of Color Center explicitly
            kotlin.run sendToColorDetails@{
                val command = ColorDetailsCommand.FetchData(color, colorRole)
                colorCenterComponents.colorDetailsCommandStore.issue(command)
            }
            kotlin.run sendToColorScheme@{
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
            true -> CanProceed.Yes(proceed = this::proceed)
            false -> CanProceed.No
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

    private fun initialDataFetchedEventProcessor(): DataFetchedEventProcessor? =
        null

    private fun onColorCenterSessionStarted(seed: Color) {
        colorCenterSessionBuilder.seed(seed)
        // recreate Color Center ViewModel (and its sub-feature ViewModels) to reset their states
        colorCenterComponentsFlow.value = ColorCenterComponents()
        kotlin.run setProcessor@{
            val currentProcessor = dataFetchedEventProcessor // capture in closure
            // implementation of a "Composite" design pattern
            dataFetchedEventProcessor = DataFetchedEventProcessor {
                with(BuildColorCenterSession()) { process() }
                dataFetchedEventProcessor = currentProcessor // restore previous value
            }
        }
        // only persist a seed of each new session
        viewModelScope.launch(defaultDispatcher) {
            lastSearchedColorRepository.setLastSearchedColor(seed)
        }
    }

    private fun onColorCenterSessionEnded() {
        colorCenterSession = null
        colorCenterSessionBuilder.clear()
        dataFetchedEventProcessor = initialDataFetchedEventProcessor()
        run disposeAndSetNull@{
            colorCenterComponentsFlow.value?.colorCenterViewModel?.dispose()
            colorCenterComponentsFlow.value = null
        }
    }

    private suspend fun updateColorInColorInput(
        color: Color,
    ) {
        withContext(uiDataUpdateDispatcher) {
            colorInputMediator.send(color = color, from = null)
        }
    }

    /**
     * A [DataFetchedEventProcessor] that creates a [ColorCenterSession]
     * and sets it into a [colorCenterSession] field.
     */
    private fun BuildColorCenterSession() =
        DataFetchedEventProcessor {
            val details = this.domainDetails
            val relatedColors = setOf(details.exact.color)
            colorCenterSession = colorCenterSessionBuilder
                .relatedColors(relatedColors)
                .build()
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
 * Specifies the way of processing (handling, reacting to) a [ColorDetailsEvent.DataFetched] event.
 * It is an implementation of a "Strategy" design pattern.
 * See [HomeViewModel.dataFetchedEventProcessor].
 */
private fun interface DataFetchedEventProcessor {
    fun ColorDetailsEvent.DataFetched.process()
}