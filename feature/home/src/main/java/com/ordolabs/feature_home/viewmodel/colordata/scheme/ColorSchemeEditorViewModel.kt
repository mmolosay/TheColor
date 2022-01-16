package com.ordolabs.feature_home.viewmodel.colordata.scheme

import androidx.lifecycle.viewModelScope
import com.ordolabs.thecolor.model.color.data.ColorSchemeRequest
import com.ordolabs.thecolor.util.MutableCommandFlow
import com.ordolabs.thecolor.util.MutableStateResourceFlow
import com.ordolabs.thecolor.util.ext.asCommand
import com.ordolabs.thecolor.util.ext.setSuccess
import com.ordolabs.thecolor.util.struct.Resource
import com.ordolabs.thecolor.util.struct.empty
import com.ordolabs.thecolor.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.asStateFlow

class ColorSchemeEditorViewModel : BaseViewModel() {

    private val _dispatchConfigCommand = MutableCommandFlow<Unit>()
    val dispatchConfigCommand = _dispatchConfigCommand.asCommand(viewModelScope)

    private val _config = MutableStateResourceFlow<ColorSchemeRequest.Config>(Resource.empty())
    val config = _config.asStateFlow()

    fun dispatchConfig(config: ColorSchemeRequest.Config) {
        _config.setSuccess(config)
        _dispatchConfigCommand.setSuccess(Unit)
    }
}