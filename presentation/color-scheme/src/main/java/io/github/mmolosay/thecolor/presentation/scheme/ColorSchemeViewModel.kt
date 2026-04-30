package io.github.mmolosay.thecolor.presentation.scheme

import arrow.optics.copy
import arrow.optics.optics
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorRepository
import io.github.mmolosay.thecolor.domain.color.ColorRepository.GetColorSchemeRequest
import io.github.mmolosay.thecolor.domain.color.ColorScheme.Mode
import io.github.mmolosay.thecolor.domain.color.IsColorLightUseCase
import io.github.mmolosay.thecolor.main.di.qualifiers.CoroutineDispatcherDiQualifiers.DefaultDispatcher
import io.github.mmolosay.thecolor.main.di.qualifiers.CoroutineDispatcherDiQualifiers.IoDispatcher
import io.github.mmolosay.thecolor.presentation.common.colorint.ColorToColorIntUseCase
import io.github.mmolosay.thecolor.presentation.common.viewmodel.SimpleViewModel
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeData.Changes
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeData.Swatch
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeData.SwatchCount
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeViewModel.Config
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeViewModel.DataState
import io.github.mmolosay.thecolor.presentation.scheme.StatefulData.State
import io.github.mmolosay.thecolor.utils.ProcessingRegistry
import io.github.mmolosay.thecolor.utils.asDelegate
import io.github.mmolosay.thecolor.utils.singleActive
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import javax.inject.Inject
import javax.inject.Singleton
import io.github.mmolosay.thecolor.domain.color.ColorScheme as DomainColorScheme

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
    @Assisted private val eventStore: ColorSchemeEventStore,
    private val colorRepository: ColorRepository,
    private val createData: CreateColorSchemeDataUseCase,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : SimpleViewModel(coroutineScope) {

    private val statefulDataFlow: MutableStateFlow<StatefulData> = run {
        val dataSession = DataSession(seed = null, domainColorScheme = null, data = null)
        val value = StatefulData(dataSession = dataSession, error = null, state = State.Idle)
        MutableStateFlow(value)
    }
    private val statefulData: StatefulData by statefulDataFlow.asDelegate()

    val dataStateFlow: StateFlow<DataState> = statefulDataFlow
        .map { it.toDataState() }
        .flowOn(defaultDispatcher)
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.Eagerly,
            initialValue = statefulDataFlow.value.toDataState(),
        )

    private val opRegistry = ProcessingRegistry<Operation>()
    private val dataEditor = ColorSchemeDataEditor(
        applyChanges = ::applyChanges,
    )

    /**
     * Fetches [DomainColorScheme] for the specified "[seed]" color of the color scheme.
     * Exposes fetched color scheme from the [dataStateFlow].
     */
    fun fetchColorScheme(seed: Color): Job =
        coroutineScope.launch(defaultDispatcher) {
            opRegistry.singleActive(
                removeAndCancelAll = { it.value is Operation.FetchColorScheme },
                value = Operation.FetchColorScheme(seed),
            ) {
                statefulDataFlow.update {
                    it.copy { StatefulData.dataSession.seed set seed }
                }
                val requestConfig = assembleRequestConfig()
                val request = requestConfig.toDomainRequest(seed)
                statefulDataFlow.update {
                    it.copy { StatefulData.state set State.Loading }
                }
                yield() // allow 'dataStateFlow' to emit 'Loading' state in unit tests
                val schemeResult = withContext(ioDispatcher) {
                    colorRepository.getColorScheme(request)
                }
                val colorScheme = schemeResult.getOrElse { exception ->
                    val tryAgain: () -> Unit = {
                        fetchColorScheme(seed)
                    }
                    val error = ColorSchemeError(
                        cause = exception,
                        tryAgain = tryAgain,
                    )
                    statefulDataFlow.update {
                        it.copy {
                            StatefulData.error set error
                            StatefulData.state set State.Error
                        }
                    }
                    return@launch
                }
                val data = createData(scheme = colorScheme, config = requestConfig)
                statefulDataFlow.update {
                    it.copy {
                        StatefulData.dataSession.domainColorScheme set colorScheme
                        StatefulData.dataSession.data set data
                        StatefulData.state set State.Ready
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
            onSwatchSelect = ::sendSwatchSelectedEvent,
            onModeSelect = ::selectMode,
            onSwatchCountSelect = ::selectSwatchCount,
        )

    private fun sendSwatchSelectedEvent(indexOfSelectedSwatch: Int) {
        val dataSession = statefulData.dataSession
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
        statefulDataFlow.update {
            val newData = with(dataEditor) {
                it.dataSession.data?.copyConsistently(selectedMode = mode)
            }
            it.copy { StatefulData.dataSession.data set newData }
        }
    }

    private fun selectSwatchCount(count: SwatchCount) {
        statefulDataFlow.update {
            val newData = with(dataEditor) {
                it.dataSession.data?.copyConsistently(selectedSwatchCount = count)
            }
            it.copy { StatefulData.dataSession.data set newData }
        }
    }

    private fun applyChanges() {
        val dataSession = statefulData.dataSession
        val data = dataSession.data ?: return
        if (data.changes !is Changes.Present) return // ignore invocations on stale state
        val seed = dataSession.seed ?: return
        fetchColorScheme(seed)
    }

    private fun assembleRequestConfig(): Config {
        fun ColorSchemeData.toConfig() =
            Config(
                mode = this.selectedMode,
                swatchCount = this.selectedSwatchCount,
            )

        fun defaultConfig() =
            Config(
                mode = Mode.Monochrome,
                swatchCount = SwatchCount.Six,
            )

        val data = statefulData.dataSession.data
        return data?.toConfig() ?: defaultConfig()
    }

    private fun Config.toDomainRequest(seed: Color): GetColorSchemeRequest =
        GetColorSchemeRequest(
            seed = seed,
            mode = this.mode,
            swatchCount = this.swatchCount.value,
        )

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
            colorSchemeEventStore: ColorSchemeEventStore,
        ): ColorSchemeViewModel
    }

    /**
     * Directly maps to the public methods of the [ColorSchemeViewModel].
     * Implements "Command" design pattern.
     */
    private sealed interface Operation {

        /**
         * Corresponds to the [ColorSchemeViewModel.fetchColorScheme] method.
         */
        data class FetchColorScheme(
            val seed: Color,
        ) : Operation
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
// 'private' but Dagger
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

/**
 * Creates updated copies of [ColorSchemeData] while ensuring data consistency.
 */
// 'private' but Dagger
class ColorSchemeDataEditor(
    private val applyChanges: () -> Unit,
) {

    fun ColorSchemeData.copyConsistently(
        selectedMode: Mode = this.selectedMode,
        selectedSwatchCount: SwatchCount = this.selectedSwatchCount,
    ): ColorSchemeData =
        this.copy(
            selectedMode = selectedMode,
            selectedSwatchCount = selectedSwatchCount,
            changes = Changes(
                selectedMode,
                this.activeMode,
                selectedSwatchCount,
                this.activeSwatchCount,
            ),
        )

    private fun Changes(
        selectedMode: Mode,
        activeMode: Mode,
        selectedSwatchCount: SwatchCount,
        activeSwatchCount: SwatchCount,
    ): Changes {
        val hasModeChanged by lazy { selectedMode != activeMode }
        val hasSwatchCountChanged by lazy { selectedSwatchCount != activeSwatchCount }
        val hasChanges = (hasModeChanged || hasSwatchCountChanged)
        return if (hasChanges) Changes.Present(applyChanges) else Changes.None
    }
}