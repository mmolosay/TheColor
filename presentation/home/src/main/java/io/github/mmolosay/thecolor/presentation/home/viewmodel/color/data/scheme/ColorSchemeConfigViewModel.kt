package io.github.mmolosay.thecolor.presentation.home.viewmodel.color.data.scheme

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.presentation.color.data.ColorScheme
import io.github.mmolosay.thecolor.presentation.color.data.ColorSchemeRequest
import io.github.mmolosay.thecolor.presentation.viewmodel.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class ColorSchemeConfigViewModel @Inject constructor(
    private val stateHandle: SavedStateHandle,
) : BaseViewModel() {

    init {
        setDefaultArgs()
    }

    var currentConfig: ColorSchemeRequest.Config
        get() = stateHandle.get<ColorSchemeRequest.Config>(KEY_CURRENT_CONFIG)!!
        set(value) = stateHandle.set(KEY_CURRENT_CONFIG, value)

    var appliedConfig: ColorSchemeRequest.Config
        get() = stateHandle.get<ColorSchemeRequest.Config>(KEY_APPLIED_CONFIG)!!
        set(value) = stateHandle.set(KEY_APPLIED_CONFIG, value)

    private fun setDefaultArgs() {
        if (stateHandle.get<ColorSchemeRequest.Config>(KEY_CURRENT_CONFIG) == null) {
            this.currentConfig = makeDefaultConfig()
        }
        if (stateHandle.get<ColorSchemeRequest.Config>(KEY_APPLIED_CONFIG) == null) {
            this.appliedConfig = makeDefaultConfig()
        }
    }

    private fun makeDefaultConfig() =
        ColorSchemeRequest.Config(
            modeOrdinal = ColorScheme.Mode.DEFAULT.ordinal,
            sampleCount = ColorSchemeRequest.Config.SAMPLE_COUNT_DEFAULT
        )

    companion object {
        private const val KEY_CURRENT_CONFIG = "KEY_CURRENT_CONFIG"
        private const val KEY_APPLIED_CONFIG = "KEY_APPLIED_CONFIG"
    }
}