package com.ordolabs.feature_home.viewmodel.colordata.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ordolabs.thecolor.model.color.ColorHexPresentation
import com.ordolabs.thecolor.model.color.ColorPresentation
import com.ordolabs.thecolor.model.color.from
import com.ordolabs.thecolor.util.MutableStateResourceFlow
import com.ordolabs.thecolor.util.ext.setSuccess
import com.ordolabs.thecolor.util.ext.shareOnceIn
import com.ordolabs.thecolor.util.struct.Resource
import com.ordolabs.thecolor.util.struct.empty

class ColorDetailsViewModel : ViewModel() {

    // TODO: implement Command class, similar to Resource?
    private val _getExactColorCommand =
        MutableStateResourceFlow<ColorPresentation>(Resource.empty())
    val getExactColorCommand = _getExactColorCommand.shareOnceIn(viewModelScope)

    override fun onCleared() {
        super.onCleared()
    }

    fun getExactColor(exactHex: String) {
        val presentation = ColorHexPresentation(value = exactHex)
        val color = ColorPresentation.from(presentation)
        _getExactColorCommand.setSuccess(color)
    }
}