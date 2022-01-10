package com.ordolabs.feature_home.viewmodel.colordata.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ordolabs.thecolor.util.ColorUtil
import com.ordolabs.thecolor.util.MutableStateResourceFlow
import com.ordolabs.thecolor.util.ext.setSuccess
import com.ordolabs.thecolor.util.ext.shareOnceIn
import com.ordolabs.thecolor.util.struct.Resource
import com.ordolabs.thecolor.util.struct.empty

class ColorDetailsViewModel : ViewModel() {

    // TODO: implement Command class, similar to Resource?
    private val _getExactColorCommand =
        MutableStateResourceFlow<ColorUtil.Color>(Resource.empty())
    val getExactColorCommand = _getExactColorCommand.shareOnceIn(viewModelScope)

    override fun onCleared() {
        super.onCleared()
    }

    fun getExactColor(exactHex: String) {
        val exact = ColorUtil.Color(exactHex)
        _getExactColorCommand.setSuccess(exact)
    }
}