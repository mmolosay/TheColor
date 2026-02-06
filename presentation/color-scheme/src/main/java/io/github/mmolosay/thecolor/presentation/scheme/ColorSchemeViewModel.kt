package io.github.mmolosay.thecolor.presentation.scheme

import arrow.atomic.update
import arrow.optics.copy
import arrow.optics.optics
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.model.ColorScheme.Mode
import io.github.mmolosay.thecolor.domain.repository.ColorRepository
import io.github.mmolosay.thecolor.domain.repository.ColorRepository.GetColorSchemeRequest
import io.github.mmolosay.thecolor.domain.result.onFailure
import io.github.mmolosay.thecolor.domain.result.onSuccess
import io.github.mmolosay.thecolor.domain.usecase.IsColorLightUseCase
import io.github.mmolosay.thecolor.presentation.common.colorint.ColorToColorIntUseCase
import io.github.mmolosay.thecolor.presentation.common.viewmodel.SimpleViewModel
import io.github.mmolosay.thecolor.presentation.errors.toErrorType
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeData.Changes
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeData.Swatch
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeData.SwatchCount
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeViewModel.Config
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeViewModel.DataState
import io.github.mmolosay.thecolor.presentation.scheme.StatefulData.State
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Named
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
    @Assisted private val eventStore: ColorSchemeEventStore,
    private val colorRepository: ColorRepository,
    private val createData: CreateColorSchemeDataUseCase,
    @Named("defaultDispatcher") private val defaultDispatcher: CoroutineDispatcher,
    @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher,
) : SimpleViewModel(coroutineScope) {

    private val statefulData: AtomicReference<StatefulData> = run {
        val dataSession = DataSession(seed = null, domainColorScheme = null, data = null)
        val value = StatefulData(dataSession = dataSession, error = null, state = State.Idle)
        AtomicReference(value)
    }

    private val fetchDataJob = AtomicReference<Job?>(null)

    private val _dataStateFlow = MutableStateFlow<DataState>(statefulData.get().toDataState())
    val dataStateFlow: StateFlow<DataState> = _dataStateFlow.asStateFlow()

    init {
        collectColorSchemeCommands()
    }

    private fun collectColorSchemeCommands() =
        coroutineScope.launch(defaultDispatcher) {
            commandProvider.commandFlow.collect { command ->
                command.process()
            }
        }

    private fun ColorSchemeCommand.process() = when (this) {
        is ColorSchemeCommand.FetchData -> {
            val seed = this.color
            statefulData.update {
                it.copy { StatefulData.dataSession.seed set seed }
            }
            fetchColorScheme(seed)
        }
    }

    private fun fetchColorScheme(seed: Color) {
        val requestConfig = assembleRequestConfig()
        val request = requestConfig.toDomainRequest(seed)
        _dataStateFlow.updateFromStateful {
            it.copy { StatefulData.state set State.Loading }
        }
        coroutineScope.launch(ioDispatcher) {
            colorRepository.getColorScheme(request)
                .onSuccess { scheme ->
                    statefulData.update {
                        it.copy { StatefulData.dataSession.domainColorScheme set scheme }
                    }
                    val data = createData(scheme = scheme, config = requestConfig)
                    _dataStateFlow.updateFromStateful {
                        it.copy {
                            StatefulData.dataSession.data set data
                            StatefulData.state set State.Ready
                        }
                    }
                }
                .onFailure { failure ->
                    val error = ColorSchemeError(
                        type = failure.toErrorType(),
                        tryAgain = ::onErrorAction,
                    )
                    _dataStateFlow.updateFromStateful {
                        it.copy {
                            StatefulData.error set error
                            StatefulData.state set State.Error
                        }
                    }
                }
        }.also { job ->
            fetchDataJob.getAndSet(job)?.cancel()
        }
    }

    private fun createData(
        scheme: DomainColorScheme,
        config: Config,
    ) =
        createData.invoke(
            scheme = scheme,
            config = config,
            onSwatchSelect = ::sendSwatchSelectedEvent,
            onModeSelect = ::selectMode,
            onSwatchCountSelect = ::selectSwatchCount,
        )

    private fun onErrorAction() {
        val seed = requireNotNull(statefulData.get().dataSession.seed)
        fetchColorScheme(seed = seed)

    }

    private fun sendSwatchSelectedEvent(indexOfSelectedSwatch: Int) {
        val dataSession = statefulData.get().dataSession
        val lastDomainColorScheme = requireNotNull(dataSession.domainColorScheme)
        val swatch =
            dataSession.data?.swatches?.getOrNull(indexOfSelectedSwatch) ?: return
        val swatchColorDetails =
            lastDomainColorScheme.swatchDetails.getOrNull(indexOfSelectedSwatch) ?: return
        val event = ColorSchemeEvent.SwatchSelected(swatch, swatchColorDetails)
        coroutineScope.launch(defaultDispatcher) {
            eventStore.send(event)
        }
    }

    private fun selectMode(mode: Mode) {
        _dataStateFlow.updateFromStateful {
            val newData = it.dataSession.data
                ?.copy { ColorSchemeData.selectedMode set mode }
                ?.harmonize()
            it.copy { StatefulData.dataSession.data set newData }
        }
    }

    private fun selectSwatchCount(count: SwatchCount) {
        _dataStateFlow.updateFromStateful {
            val newData = it.dataSession.data
                ?.copy { ColorSchemeData.selectedSwatchCount set count }
                ?.harmonize()
            it.copy { StatefulData.dataSession.data set newData }
        }
    }

    private fun applyChanges() {
        val stateful = statefulData.get()
        val data = stateful.dataSession.data ?: return
        if (data.changes !is Changes.Present) return // ignore clicks during button hiding animation
        val seed = stateful.dataSession.seed ?: return
        fetchColorScheme(seed)
    }

    private fun assembleRequestConfig(): Config {
        val data = statefulData.get().dataSession.data
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

    private fun Config.toDomainRequest(seed: Color): GetColorSchemeRequest =
        GetColorSchemeRequest(
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

    private fun MutableStateFlow<DataState>.updateFromStateful(
        update: (StatefulData) -> StatefulData,
    ) {
        val newData = statefulData.updateAndGet(update)
        this.value = newData.toDataState()
    }

    /** [GetColorSchemeRequest] mapped to presentation layer model. */
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
            colorSchemeEventStore: ColorSchemeEventStore,
        ): ColorSchemeViewModel
    }

    companion object {
        private val InitialOrFallbackMode = Mode.Monochrome
        private val InitialOrFallbackSwatchCount = SwatchCount.Six
    }
}

/**
 * Couples data which is exposed from the ViewModel with various values that are related to it:
 * derived from the exposed data, or used to produce it.
 */
@optics
// 'private' but optics
data class DataSession(
    val seed: Color?,
    val domainColorScheme: DomainColorScheme?,
    val data: ColorSchemeData?,
) {
    companion object // required by Arrow's optics
}

/**
 * Couples data with a state.
 * This class is mapped to [DataState] (see [toDataState()][toDataState]).
 * Such approach solves issue of losing last [data], e.g. when [DataState] changes from `Ready` to `Loading`.
 */
@optics
// 'private' but optics
data class StatefulData(
    val dataSession: DataSession,
    val error: ColorSchemeError?,
    val state: State,
) {

    enum class State {
        Idle, Loading, Ready, Error,
    }

    companion object // required by Arrow's optics
}

private fun StatefulData.toDataState(): DataState =
    when (this.state) {
        State.Idle -> DataState.Idle
        State.Loading -> DataState.Loading
        State.Ready -> DataState.Ready(data = requireNotNull(this.dataSession.data))
        State.Error -> DataState.Error(error = requireNotNull(this.error))
    }

@Singleton
class CreateColorSchemeDataUseCase @Inject constructor(
    private val colorToColorInt: ColorToColorIntUseCase,
    private val isColorLight: IsColorLightUseCase,
) {

    operator fun invoke(
        scheme: DomainColorScheme,
        config: Config,
        onSwatchSelect: (indexOfSwatch: Int) -> Unit,
        onModeSelect: (Mode) -> Unit,
        onSwatchCountSelect: (SwatchCount) -> Unit,
    ) =
        ColorSchemeData(
            swatches = scheme.swatchDetails.map { details ->
                details.color.toSwatch()
            },
            onSwatchSelect = onSwatchSelect,
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