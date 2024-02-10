package io.github.mmolosay.thecolor.presentation.scheme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.model.ColorScheme
import io.github.mmolosay.thecolor.domain.model.ColorScheme.Mode
import io.github.mmolosay.thecolor.domain.usecase.ColorConverter
import io.github.mmolosay.thecolor.domain.usecase.GetColorSchemeUseCase
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeData.ApplyChangesButton
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeData.ColorInt
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeData.SwatchCount
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class ColorSchemeViewModel @Inject constructor(
    private val getColorScheme: GetColorSchemeUseCase,
    private val colorConverter: ColorConverter,
    @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _dataState = MutableStateFlow<State>(State.Loading)
    val dataState = _dataState.asStateFlow()

    fun getColorScheme(seed: Color) {
        val request = assembleRequestFromCurrentData(seed)
        _dataState.value = State.Loading
        viewModelScope.launch(ioDispatcher) {
            val scheme = getColorScheme(request)
            val data = ColorSchemeData(scheme, request)
            _dataState.value = State.Ready(data)
        }
    }

    private fun onModeSelect(mode: Mode) {
        val data = dataState.value.asReadyOrNull()?.data ?: return
        _dataState.value = data.smartCopy(selectedMode = mode).let { State.Ready(it) }
    }

    private fun onSwatchCountSelect(count: SwatchCount) {
        val data = dataState.value.asReadyOrNull()?.data ?: return
        _dataState.value = data.smartCopy(selectedSwatchCount = count).let { State.Ready(it) }
    }

    private fun onApplyChangesClick() {
        val data = dataState.value.asReadyOrNull()?.data ?: return
        if (data.applyChangesButton !is ApplyChangesButton.Visible) return // ignore clicks during button hiding animation
        getColorScheme(seed = Color.Hex(0x1A803F)) // TODO: use real color
    }

    private fun assembleRequestFromCurrentData(seed: Color): GetColorSchemeUseCase.Request {
        val data = dataState.value.asReadyOrNull()?.data
        return GetColorSchemeUseCase.Request(
            seed = seed,
            mode = data?.selectedMode ?: InitialOrFallbackMode,
            swatchCount = (data?.selectedSwatchCount ?: InitialOrFallbackSwatchCount).value,
        )
    }

    private fun ColorSchemeData(
        scheme: ColorScheme,
        request: GetColorSchemeUseCase.Request,
    ): ColorSchemeData =
        ColorSchemeData(
            swatches = scheme.swatchDetails.map { ColorInt(it.color) },
            activeMode = request.mode,
            selectedMode = request.mode,
            onModeSelect = ::onModeSelect,
            activeSwatchCount = SwatchCount.entries.first { it.value == request.swatchCount }, // TODO: refactor
            selectedSwatchCount = SwatchCount.entries.first { it.value == request.swatchCount }, // TODO: refactor
            onSwatchCountSelect = ::onSwatchCountSelect,
            applyChangesButton = ApplyChangesButton(areThereChangesToApply = false), // initial selected config is equal to the active one
        )

    private fun ColorInt(color: Color): ColorInt {
        val hex = with(colorConverter) { color.toHex() }
        return ColorInt(hex = hex.value)
    }

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

    sealed interface State {
        data object Loading : State
        data class Ready(val data: ColorSchemeData) : State
    }

    companion object {
        private val InitialOrFallbackMode = Mode.Monochrome
        private val InitialOrFallbackSwatchCount = SwatchCount.Six
    }
}