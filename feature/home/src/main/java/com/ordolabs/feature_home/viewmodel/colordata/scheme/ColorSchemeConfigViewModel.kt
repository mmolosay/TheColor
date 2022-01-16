package com.ordolabs.feature_home.viewmodel.colordata.scheme

import androidx.lifecycle.viewModelScope
import com.ordolabs.thecolor.model.color.data.ColorScheme
import com.ordolabs.thecolor.model.color.data.ColorSchemeRequest
import com.ordolabs.thecolor.util.MutableCommandFlow
import com.ordolabs.thecolor.util.ext.asCommand
import com.ordolabs.thecolor.util.ext.setSuccess
import com.ordolabs.thecolor.viewmodel.BaseViewModel

/**
 * Stores [ColorSchemeRequest.Config] parameters and assembles new instances of `Config`.
 */
class ColorSchemeConfigViewModel : BaseViewModel() {

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

    var appliedConfig: ColorSchemeRequest.Config = assembleConfig(); private set

    private val _hasChangesCommand = MutableCommandFlow(false)
    val hasChangesCommand = _hasChangesCommand.asCommand(viewModelScope)

    /**
     * Assembles new [ColorSchemeRequest.Config] from current values,
     * sets it in [appliedConfig] and returns it.
     */
    fun applyConfig(): ColorSchemeRequest.Config =
        assembleConfig().also { config ->
            this.appliedConfig = config
            _hasChangesCommand.setSuccess(false) // just applied, thus the same
        }

    private fun updateHasChangesCommand() {
        val currentConfig = assembleConfig()
        val hasChanges = (currentConfig != appliedConfig)
        _hasChangesCommand.setSuccess(hasChanges)
    }

    private fun assembleConfig() =
        ColorSchemeRequest.Config(
            mode.ordinal,
            sampleCount
        )
}