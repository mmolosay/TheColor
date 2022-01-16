package com.ordolabs.feature_home.viewmodel.colordata.scheme

import com.ordolabs.thecolor.model.color.Color
import com.ordolabs.thecolor.model.color.data.ColorSchemeRequest
import com.ordolabs.thecolor.util.MutableStateResourceFlow
import com.ordolabs.thecolor.util.ext.setSuccess
import com.ordolabs.thecolor.util.struct.getOrNull
import com.ordolabs.thecolor.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.asStateFlow

class ColorSchemeSettingsViewModel(
    private val seed: Color
) : BaseViewModel() {

    private val _schemeModeOrdinal = MutableStateResourceFlow(0)
    val schemeModeOrdinal = _schemeModeOrdinal.asStateFlow()

    fun updateSchemeModeOrdinal(ordinal: Int) {
        _schemeModeOrdinal.setSuccess(ordinal)
    }

    fun assembleColorSchemeRequest() =
        ColorSchemeRequest(
            seed,
            schemeModeOrdinal.value.getOrNull()!!,
            8
        )
}