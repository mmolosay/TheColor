package com.ordolabs.feature_home.viewmodel.colordata.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ordolabs.thecolor.model.color.Color
import com.ordolabs.thecolor.model.color.ColorPrototype
import com.ordolabs.thecolor.model.color.from
import com.ordolabs.thecolor.util.MutableCommandFlow
import com.ordolabs.thecolor.util.ext.asCommand
import com.ordolabs.thecolor.util.ext.setSuccess
import javax.inject.Inject

// TODO: derive from BaseViewModel, not ViewModel
class ColorDetailsViewModel @Inject constructor() : ViewModel() {

    private val _getExactColorCommand = MutableCommandFlow<Color>()
    val getExactColorCommand = _getExactColorCommand.asCommand(viewModelScope)

    override fun onCleared() {
        super.onCleared()
    }

    fun getExactColor(exactHex: String) {
        val proto = ColorPrototype.Hex(value = exactHex)
        val color = Color.from(proto)!! // exactHex is always valid
        _getExactColorCommand.setSuccess(color)
    }
}