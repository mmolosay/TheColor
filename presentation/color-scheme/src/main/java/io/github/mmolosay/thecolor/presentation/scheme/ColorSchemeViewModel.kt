package io.github.mmolosay.thecolor.presentation.scheme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.model.ColorScheme.Mode
import io.github.mmolosay.thecolor.domain.usecase.ColorConverter
import io.github.mmolosay.thecolor.domain.usecase.GetColorSchemeUseCase
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeData.ApplyChangesButton
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeData.ColorInt
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeData.SwatchCount
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeViewModel.Actions
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeViewModel.Config
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeViewModel.State
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import io.github.mmolosay.thecolor.domain.model.ColorScheme as DomainColorScheme

@HiltViewModel
class ColorSchemeViewModel @Inject constructor(
    getInitialState: GetInitialStateUseCase,
    private val getColorScheme: GetColorSchemeUseCase,
    private val colorSchemeDataFactory: ColorSchemeDataFactory,
    @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val actions = Actions()

    private val _dataStateFlow = MutableStateFlow(getInitialState(actions))
    val dataStateFlow = _dataStateFlow.asStateFlow()

    // TODO: never changes, ViewModel is created for a particular color; refactor to injected ColorProvider
    private var lastUsedSeed: Color? = null

    fun getColorScheme(seed: Color) {
        val requestConfig = assembleRequestConfigFromCurrentData()
        val request = requestConfig.toDomainRequest(seed)
        _dataStateFlow.value = State.Loading
        lastUsedSeed = seed
        viewModelScope.launch(ioDispatcher) {
            val scheme = getColorScheme(request)
            val data = colorSchemeDataFactory.create(
                scheme = scheme,
                config = requestConfig,
                actions = actions,
            )
            _dataStateFlow.value = State.Ready(data)
        }
    }

    private fun onModeSelect(mode: Mode) {
        val data = dataStateFlow.value.asReadyOrNull()?.data ?: return
        _dataStateFlow.value = data.smartCopy(selectedMode = mode).let { State.Ready(it) }
    }

    private fun onSwatchCountSelect(count: SwatchCount) {
        val data = dataStateFlow.value.asReadyOrNull()?.data ?: return
        _dataStateFlow.value = data.smartCopy(selectedSwatchCount = count).let { State.Ready(it) }
    }

    private fun onApplyChangesClick() {
        val data = dataStateFlow.value.asReadyOrNull()?.data ?: return
        if (data.applyChangesButton !is ApplyChangesButton.Visible) return // ignore clicks during button hiding animation
        val seed = lastUsedSeed ?: return
        getColorScheme(seed)
    }

    private fun assembleRequestConfigFromCurrentData(): Config {
        val data = dataStateFlow.value.asReadyOrNull()?.data
        return Config(
            mode = data?.selectedMode ?: InitialOrFallbackMode,
            swatchCount = data?.selectedSwatchCount ?: InitialOrFallbackSwatchCount,
        )
    }

    private fun Config.toDomainRequest(seed: Color): GetColorSchemeUseCase.Request =
        GetColorSchemeUseCase.Request(
            seed = seed,
            mode = this.mode,
            swatchCount = this.swatchCount.value,
        )

    private fun ApplyChangesButton(areThereChangesToApply: Boolean): ApplyChangesButton =
        if (areThereChangesToApply) {
            ApplyChangesButton.Visible(
                onClick = ::onApplyChangesClick,
            )
        } else {
            ApplyChangesButton.Hidden
        }

    private fun ColorSchemeData.smartCopy(
        selectedMode: Mode = this.selectedMode,
        selectedSwatchCount: SwatchCount = this.selectedSwatchCount,
    ): ColorSchemeData {
        fun hasModeChanged() = (selectedMode != this.activeMode)
        fun hasSwatchCountChanged() = (selectedSwatchCount != this.activeSwatchCount)
        val areThereChangesToApply = (hasModeChanged() || hasSwatchCountChanged())
        return this.copy(
            selectedMode = selectedMode,
            selectedSwatchCount = selectedSwatchCount,
            applyChangesButton = ApplyChangesButton(areThereChangesToApply),
        )
    }

    private fun State.asReadyOrNull() =
        this as? State.Ready

    /** [GetColorSchemeUseCase.Request] mapped to presentation layer model. */
    data class Config(
        val mode: Mode,
        val swatchCount: SwatchCount,
    )

    inner class Actions(
        val onModeSelect: (Mode) -> Unit = ::onModeSelect,
        val onSwatchCountSelect: (SwatchCount) -> Unit = ::onSwatchCountSelect,
        val onApplyChangesClick: () -> Unit = ::onApplyChangesClick,
    )

    sealed interface State {
        data object Loading : State
        data class Ready(val data: ColorSchemeData) : State
    }

    companion object {
        private val InitialOrFallbackMode = Mode.Monochrome
        private val InitialOrFallbackSwatchCount = SwatchCount.Six
    }
}

@Singleton
@Suppress("UNUSED_PARAMETER") // used in tests
class GetInitialStateUseCase @Inject constructor() {
    operator fun invoke(actions: Actions): State =
        State.Loading
}

@Singleton
class ColorSchemeDataFactory @Inject constructor(
    private val colorConverter: ColorConverter,
) {

    fun create(
        scheme: DomainColorScheme,
        config: Config,
        actions: Actions,
    ) =
        ColorSchemeData(
            swatches = scheme.swatchDetails.map { it.color.toColorInt() },
            activeMode = config.mode,
            selectedMode = config.mode,
            onModeSelect = actions.onModeSelect,
            activeSwatchCount = config.swatchCount,
            selectedSwatchCount = config.swatchCount,
            onSwatchCountSelect = actions.onSwatchCountSelect,
            applyChangesButton = ApplyChangesButton.Hidden, // selected and active configs are the same, no changes to apply
        )

    private fun Color.toColorInt(): ColorInt {
        val color = this
        val hex = with(colorConverter) { color.toHex() }
        return ColorInt(hex = hex.value)
    }
}