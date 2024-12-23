package io.github.mmolosay.thecolor.presentation.scheme

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.model.ColorScheme.Mode
import io.github.mmolosay.thecolor.domain.result.onFailure
import io.github.mmolosay.thecolor.domain.result.onSuccess
import io.github.mmolosay.thecolor.domain.usecase.GetColorSchemeUseCase
import io.github.mmolosay.thecolor.domain.usecase.IsColorLightUseCase
import io.github.mmolosay.thecolor.presentation.api.ColorToColorIntUseCase
import io.github.mmolosay.thecolor.presentation.api.SimpleViewModel
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsCommand
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsCommandStore
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsEvent
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsEventStore
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsViewModel
import io.github.mmolosay.thecolor.presentation.errors.toErrorType
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeData.Changes
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeData.Swatch
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeData.SwatchCount
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeViewModel.Config
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeViewModel.DataState
import io.github.mmolosay.thecolor.presentation.scheme.StatefulData.State
import io.github.mmolosay.thecolor.utils.doNothing
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton
import io.github.mmolosay.thecolor.domain.model.ColorScheme as DomainColorScheme

/**
 * Handles presentation logic of the 'Color Scheme' feature.
 *
 * Unlike typical `ViewModel`s, it doesn't derive from Google's [ViewModel][androidx.lifecycle.ViewModel],
 * thus cannot be instantiated using [ViewModelProvider][androidx.lifecycle.ViewModelProvider].
 *
 * Instead, it can be created within "simple" `ViewModel` or Google's `ViewModel`.
 */
class ColorSchemeViewModel @AssistedInject constructor(
    @Assisted coroutineScope: CoroutineScope,
    @Assisted private val commandProvider: ColorSchemeCommandProvider,
    colorDetailsCommandStoreProvider: Provider<ColorDetailsCommandStore>,
    colorDetailsEventStoreProvider: Provider<ColorDetailsEventStore>,
    colorDetailsViewModelFactory: ColorDetailsViewModel.Factory,
    private val getColorScheme: GetColorSchemeUseCase,
    private val createData: CreateColorSchemeDataUseCase,
    @Named("defaultDispatcher") private val defaultDispatcher: CoroutineDispatcher,
    @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher,
) : SimpleViewModel(coroutineScope) {

    private val selectedSwatchDetailsCommandStore = colorDetailsCommandStoreProvider.get()
    private val selectedSwatchDetailsEventStore = colorDetailsEventStoreProvider.get()

    val selectedSwatchDetailsViewModel = colorDetailsViewModelFactory.create(
        coroutineScope = coroutineScope,
        colorDetailsCommandProvider = selectedSwatchDetailsCommandStore,
        colorDetailsEventStore = selectedSwatchDetailsEventStore,
    )

    private var lastUsedSeed: Color? = null
    private var lastDomainColorScheme: DomainColorScheme? = null

    private val _statefulDataFlow = MutableStateFlow(initialStatefulData())
    val dataStateFlow: StateFlow<DataState> = _statefulDataFlow
        .map { it.toDataState() }
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.Eagerly, // View will start collecting immediately, also simplifies tests
            initialValue = DataState.Loading,
        )

    init {
        collectColorCenterCommands()
        collectSelectedSwatchDetailsEvents()
    }

    private fun collectColorCenterCommands() =
        coroutineScope.launch(defaultDispatcher) {
            commandProvider.commandFlow.collect { command ->
                command.process()
            }
        }

    private fun collectSelectedSwatchDetailsEvents() =
        coroutineScope.launch(defaultDispatcher) {
            selectedSwatchDetailsEventStore.eventFlow.collect { event ->
                when (event) {
                    is ColorDetailsEvent.ColorSelected -> {
                        val command = ColorDetailsCommand.FetchData(
                            color = event.color,
                            colorRole = event.colorRole,
                        )
                        selectedSwatchDetailsCommandStore.issue(command)
                    }
                    else -> doNothing()
                }
            }
        }

    private fun ColorSchemeCommand.process() = when (this) {
        is ColorSchemeCommand.FetchData -> {
            val seed = this.color
            lastUsedSeed = seed
            fetchColorScheme(seed)
        }
    }

    private fun fetchColorScheme(seed: Color) {
        val requestConfig = assembleRequestConfig()
        val request = requestConfig.toDomainRequest(seed)
        _statefulDataFlow.updateState(State.Loading)
        coroutineScope.launch(ioDispatcher) {
            getColorScheme(request)
                .onSuccess { scheme ->
                    lastDomainColorScheme = scheme
                    val data = createData(scheme = scheme, config = requestConfig)
                    _statefulDataFlow.update {
                        it.copy(data = data, state = State.Ready)
                    }
                }
                .onFailure { failure ->
                    val error = ColorSchemeError(
                        type = failure.toErrorType(),
                        tryAgain = ::onErrorAction,
                    )
                    _statefulDataFlow.update {
                        it.copy(error = error, state = State.Error)
                    }
                }
        }
    }

    private fun createData(
        scheme: DomainColorScheme,
        config: Config,
    ) =
        createData.invoke(
            scheme = scheme,
            config = config,
            onSwatchSelect = ::onSwatchSelect,
            onSelectedSwatchDismiss = ::onSelectedSwatchDismiss,
            onModeSelect = ::selectMode,
            onSwatchCountSelect = ::selectSwatchCount,
        )

    private fun onErrorAction() {
        val seed = requireNotNull(lastUsedSeed)
        fetchColorScheme(seed = seed)
    }

    private fun onSwatchSelect(index: Int) {
        // explicit exception is better for monitoring crashes and finding possible bugs
        require(dataStateFlow.value is DataState.Ready)
        val lastDomainColorScheme = requireNotNull(lastDomainColorScheme)
        val selectedSwatchData = lastDomainColorScheme.swatchDetails[index]
        coroutineScope.launch(defaultDispatcher) {
            val command = ColorDetailsCommand.SetColorDetails(domainDetails = selectedSwatchData)
            selectedSwatchDetailsCommandStore.issue(command)
        }
        _statefulDataFlow.updateData {
            it?.copy(isAnySwatchSelected = true)
        }
    }

    private fun onSelectedSwatchDismiss() {
        _statefulDataFlow.updateData {
            it?.copy(isAnySwatchSelected = false)
        }
    }

    private fun selectMode(mode: Mode) {
        _statefulDataFlow.updateNotNullData { currentData ->
            currentData
                .copy(selectedMode = mode)
                .harmonize()
        }
    }

    private fun selectSwatchCount(count: SwatchCount) {
        _statefulDataFlow.updateNotNullData { currentData ->
            currentData
                .copy(selectedSwatchCount = count)
                .harmonize()
        }
    }

    private fun applyChanges() {
        val data = _statefulDataFlow.value.data ?: return
        if (data.changes !is Changes.Present) return // ignore clicks during button hiding animation
        val seed = lastUsedSeed ?: return
        fetchColorScheme(seed)
    }

    private fun assembleRequestConfig(): Config {
        val data = _statefulDataFlow.value.data
        return if (data != null)
            Config(
                mode = data.selectedMode,
                swatchCount = data.selectedSwatchCount,
            )
        else
            Config(
                mode = InitialOrFallbackMode,
                swatchCount = InitialOrFallbackSwatchCount,
            )
    }

    private fun Config.toDomainRequest(seed: Color): GetColorSchemeUseCase.Request =
        GetColorSchemeUseCase.Request(
            seed = seed,
            mode = this.mode,
            swatchCount = this.swatchCount.value,
        )

    /** Synchronizes values between each other, ensuring data integrity. */
    private fun ColorSchemeData.harmonize(): ColorSchemeData {
        val changes = run {
            fun hasModeChanged() = (selectedMode != activeMode)
            fun hasSwatchCountChanged() = (selectedSwatchCount != activeSwatchCount)
            val hasChanges = (hasModeChanged() || hasSwatchCountChanged())
            if (hasChanges) {
                Changes.Present(applyChanges = ::applyChanges)
            } else {
                Changes.None
            }
        }
        return this.copy(
            changes = changes,
        )
    }

    private fun initialStatefulData() =
        StatefulData(
            data = null,
            error = null,
            state = State.Idle,
        )

    /** [GetColorSchemeUseCase.Request] mapped to presentation layer model. */
    data class Config(
        val mode: Mode,
        val swatchCount: SwatchCount,
    )

    sealed interface DataState {
        data object Idle : DataState
        data object Loading : DataState
        data class Ready(val data: ColorSchemeData) : DataState
        data class Error(val error: ColorSchemeError) : DataState
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            coroutineScope: CoroutineScope,
            colorSchemeCommandProvider: ColorSchemeCommandProvider,
        ): ColorSchemeViewModel
    }

    companion object {
        private val InitialOrFallbackMode = Mode.Monochrome
        private val InitialOrFallbackSwatchCount = SwatchCount.Six
    }
}

/**
 * Couples data with a state.
 * This class is mapped to [DataState] (see [toDataState()][toDataState]).
 * Such approach solves issue of losing last [data] when [DataState] changes from `Ready` to `Loading`.
 */
private data class StatefulData(
    val data: ColorSchemeData?,
    val error: ColorSchemeError?,
    val state: State,
) {
    enum class State {
        Idle, Loading, Ready, Error,
    }
}

private fun StatefulData.toDataState(): DataState =
    when (this.state) {
        State.Idle -> DataState.Idle
        State.Loading -> DataState.Loading
        State.Ready -> DataState.Ready(data = requireNotNull(this.data))
        State.Error -> DataState.Error(error = requireNotNull(this.error))
    }

private fun MutableStateFlow<StatefulData>.updateState(
    newState: State,
) =
    update {
        it.copy(state = newState)
    }

private fun MutableStateFlow<StatefulData>.updateData(
    createNewData: (currentData: ColorSchemeData?) -> ColorSchemeData?,
) =
    update { statefulData ->
        val currentData = statefulData.data
        val newData = createNewData(currentData)
        statefulData.copy(data = newData)
    }

private fun MutableStateFlow<StatefulData>.updateNotNullData(
    createNewData: (currentData: ColorSchemeData) -> ColorSchemeData,
) =
    updateData { currentData ->
        if (currentData == null) return@updateData null
        createNewData(currentData)
    }

@Singleton
class CreateColorSchemeDataUseCase @Inject constructor(
    private val colorToColorInt: ColorToColorIntUseCase,
    private val isColorLight: IsColorLightUseCase,
) {

    operator fun invoke(
        scheme: DomainColorScheme,
        config: Config,
        onSwatchSelect: (index: Int) -> Unit,
        onSelectedSwatchDismiss: () -> Unit,
        onModeSelect: (Mode) -> Unit,
        onSwatchCountSelect: (SwatchCount) -> Unit,
    ) =
        ColorSchemeData(
            swatches = scheme.swatchDetails.map { details ->
                details.color.toSwatch()
            },
            onSwatchSelect = onSwatchSelect,
            onSelectedSwatchDismiss = onSelectedSwatchDismiss,
            isAnySwatchSelected = false, // initially there's no selected swatch
            activeMode = config.mode,
            selectedMode = config.mode,
            onModeSelect = onModeSelect,
            activeSwatchCount = config.swatchCount,
            selectedSwatchCount = config.swatchCount,
            onSwatchCountSelect = onSwatchCountSelect,
            changes = Changes.None, // 'active' and 'selected' values are same initially
        )

    private fun Color.toSwatch() =
        Swatch(
            color = with(colorToColorInt) { toColorInt() },
            isDark = with(isColorLight) { isLight().not() },
        )
}