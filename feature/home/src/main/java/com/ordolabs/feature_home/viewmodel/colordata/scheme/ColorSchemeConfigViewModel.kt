package com.ordolabs.feature_home.viewmodel.colordata.scheme

import androidx.lifecycle.viewModelScope
import com.ordolabs.thecolor.model.color.data.ColorScheme
import com.ordolabs.thecolor.model.color.data.ColorSchemeRequest
import com.ordolabs.thecolor.util.MutableCommandFlow
import com.ordolabs.thecolor.util.MutableStateResourceFlow
import com.ordolabs.thecolor.util.ext.asCommand
import com.ordolabs.thecolor.util.ext.setSuccess
import com.ordolabs.thecolor.util.struct.getOrNull
import com.ordolabs.thecolor.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Stores [ColorSchemeRequest.Config] parameters and assembles new instances of `Config`.
 */
class ColorSchemeConfigViewModel @Inject constructor() : BaseViewModel() {

    var mode: ColorScheme.Mode = ColorScheme.Mode.DEFAULT
        set(value) {
            field = value
            updateHasChangesCommand()
        }

    var sampleCount: Int = ColorSchemeRequest.Config.SAMPLE_COUNT_DEFAULT
        set(value) {
            field = value
            updateHasChangesCommand()
        }

    private val _appliedConfig = MutableStateResourceFlow(assembleConfig())
    val appliedConfig = _appliedConfig.asStateFlow()

    private val _hasChangesCommand = MutableCommandFlow(false)
    val hasChangesCommand = _hasChangesCommand.asCommand(viewModelScope)

    /**
     * Assembles new [ColorSchemeRequest.Config] from current values,
     * sets it in [appliedConfig] and returns it.
     */
    fun applyConfig(): ColorSchemeRequest.Config =
        assembleConfig().also { config ->
            _appliedConfig.setSuccess(config)
            _hasChangesCommand.setSuccess(false) // just applied, thus the same
        }

    private fun updateHasChangesCommand() {
        val current = assembleConfig()
        val applied = _appliedConfig.value.getOrNull()!! // should always be success
        val hasChanges = (current != applied)
        _hasChangesCommand.setSuccess(hasChanges)
    }

    private fun assembleConfig() =
        ColorSchemeRequest.Config(
            mode.ordinal,
            sampleCount
        )
}