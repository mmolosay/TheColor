package io.github.mmolosay.thecolor.presentation.scheme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.model.ColorScheme.Mode
import io.github.mmolosay.thecolor.domain.usecase.GetColorSchemeUseCase
import io.github.mmolosay.thecolor.domain.usecase.IsColorLightUseCase
import io.github.mmolosay.thecolor.presentation.ColorCenterCommandProvider
import io.github.mmolosay.thecolor.presentation.ColorToColorIntUseCase
import io.github.mmolosay.thecolor.presentation.ColorCenterCommand
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeData.Changes
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeData.Swatch
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeData.SwatchCount
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeViewModel.Config
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeViewModel.State
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import io.github.mmolosay.thecolor.domain.model.ColorScheme as DomainColorScheme

internal typealias ModelsState = State<ColorSchemeData.Models>
internal typealias DataState = State<ColorSchemeData>

@HiltViewModel
class ColorSchemeViewModel @Inject constructor(
    getInitialModelsState: GetInitialModelsStateUseCase,
    private val commandProvider: ColorCenterCommandProvider,
    private val getColorScheme: GetColorSchemeUseCase,
    private val createModels: CreateDataModelsUseCase,
    @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val modelsStateFlow = MutableStateFlow(getInitialModelsState())

    val dataStateFlow: StateFlow<DataState> = modelsStateFlow
        .map { state ->
            state.mapType { models -> ColorSchemeData(models) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly, // View will start collecting immediately, also simplifies tests
            initialValue = State.Loading,
        )

    private var lastUsedSeed: Color? = null

    init {
        collectColorCenterCommands()
    }

    private fun collectColorCenterCommands() =
        viewModelScope.launch { // TODO: not main dispatcher?
            commandProvider.commandFlow.collect { command ->
                when (command) {
                    is ColorCenterCommand.FetchData -> onFetchDataCommand(command)
                }
            }
        }

    private fun onFetchDataCommand(command: ColorCenterCommand.FetchData) {
        val seed = command.color
        lastUsedSeed = seed
        getColorScheme(seed)
    }

    private fun getColorScheme(seed: Color) {
        val requestConfig = assembleRequestConfig()
        val request = requestConfig.toDomainRequest(seed)
        modelsStateFlow.value = State.Loading
        viewModelScope.launch(ioDispatcher) {
            val scheme = getColorScheme(request)
            val models = createModels(scheme = scheme, config = requestConfig)
            modelsStateFlow.value = State.Ready(models)
        }
    }

    // TODO: add unit tests
    private fun goToSwatchDetails(index: Int) {
        // TODO: implement
    }

    private fun selectMode(mode: Mode) {
        val models = modelsStateFlow.value.asReadyOrNull()?.data ?: return
        val newModels = models.copy(selectedMode = mode)
        modelsStateFlow.value = State.Ready(newModels)
    }

    private fun selectSwatchCount(count: SwatchCount) {
        val models = modelsStateFlow.value.asReadyOrNull()?.data ?: return
        val newModels = models.copy(selectedSwatchCount = count)
        modelsStateFlow.value = State.Ready(newModels)
    }

    private fun applyChanges() {
        val data = dataStateFlow.value.asReadyOrNull()?.data ?: return
        if (data.changes !is Changes.Present) return // ignore clicks during button hiding animation
        val seed = lastUsedSeed ?: return
        getColorScheme(seed)
    }

    private fun assembleRequestConfig(): Config {
        val data = dataStateFlow.value.asReadyOrNull()?.data
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

    private fun ColorSchemeData(models: ColorSchemeData.Models) =
        ColorSchemeData(
            swatches = models.swatches,
            onSwatchClick = ::goToSwatchDetails,
            activeMode = models.activeMode,
            selectedMode = models.selectedMode,
            onModeSelect = ::selectMode,
            activeSwatchCount = models.activeSwatchCount,
            selectedSwatchCount = models.selectedSwatchCount,
            onSwatchCountSelect = ::selectSwatchCount,
            changes = Changes(models),
        )

    private fun Changes(models: ColorSchemeData.Models): Changes {
        fun hasModeChanged() = with(models) { (selectedMode != activeMode) }
        fun hasSwatchCountChanged() = with(models) { (selectedSwatchCount != activeSwatchCount) }
        val hasChanges = (hasModeChanged() || hasSwatchCountChanged())
        return if (hasChanges) {
            Changes.Present(applyChanges = ::applyChanges)
        } else {
            Changes.None
        }
    }

    private fun <T> State<T>.asReadyOrNull() =
        this as? State.Ready

    private fun <T, R> State<T>.mapType(transform: (T) -> R): State<R> =
        when (this) {
            is State.Loading -> this
            is State.Ready -> transform(data).let { State.Ready(it) }
        }

    /** [GetColorSchemeUseCase.Request] mapped to presentation layer model. */
    data class Config(
        val mode: Mode,
        val swatchCount: SwatchCount,
    )

    sealed interface State<out T> {
        data object Loading : State<Nothing>
        data class Ready<T>(val data: T) : State<T>
    }

    companion object {
        private val InitialOrFallbackMode = Mode.Monochrome
        private val InitialOrFallbackSwatchCount = SwatchCount.Six
    }
}

/**
 * Exists to make unit testing easier.
 * Replaces a long chain of actions that set [ColorSchemeViewModel.dataStateFlow] in "given" part
 * of the test to required value.
 */
@Singleton
class GetInitialModelsStateUseCase @Inject constructor() {
    operator fun invoke(): ModelsState =
        State.Loading
}

/** Maps [DomainColorScheme] to presentation layer [ColorSchemeData.Models]. */
@Singleton
class CreateDataModelsUseCase @Inject constructor(
    private val colorToColorInt: ColorToColorIntUseCase,
    private val isColorLight: IsColorLightUseCase,
) {

    operator fun invoke(
        scheme: DomainColorScheme,
        config: Config,
    ) =
        ColorSchemeData.Models(
            swatches = scheme.swatchDetails.map { details ->
                details.color.toSwatch()
            },
            activeMode = config.mode,
            selectedMode = config.mode,
            activeSwatchCount = config.swatchCount,
            selectedSwatchCount = config.swatchCount,
        )

    private fun Color.toSwatch() =
        Swatch(
            color = with(colorToColorInt) { toColorInt() },
            isDark = with(isColorLight) { isLight().not() },
        )
}