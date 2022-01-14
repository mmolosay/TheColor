package com.ordolabs.feature_home.viewmodel.colordata.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ordolabs.thecolor.model.color.Color
import com.ordolabs.thecolor.model.color.ColorPrototype
import com.ordolabs.thecolor.model.color.from
import com.ordolabs.thecolor.util.MutableStateResourceFlow
import com.ordolabs.thecolor.util.ext.setSuccess
import com.ordolabs.thecolor.util.ext.shareOnceIn
import com.ordolabs.thecolor.util.struct.Resource
import com.ordolabs.thecolor.util.struct.empty

class ColorDetailsViewModel : ViewModel() {

    // TODO: implement Command class, similar to Resource?
    private val _getExactColorCommand =
        MutableStateResourceFlow<Color>(Resource.empty())
    val getExactColorCommand = _getExactColorCommand.shareOnceIn(viewModelScope)

    override fun onCleared() {
        super.onCleared()
    }

    fun getExactColor(exactHex: String) {
        val proto = ColorPrototype.Hex(value = exactHex)
        val color = Color.from(proto)!! // exactHex is always valid
        _getExactColorCommand.setSuccess(color)
    }
}