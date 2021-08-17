package com.ordolabs.thecolor.viewmodel

import com.ordolabs.thecolor.model.ColorHexPresentation
import com.ordolabs.thecolor.model.ColorRgbPresentation
import kotlinx.coroutines.CoroutineExceptionHandler

class ColorInputViewModel : BaseViewModel() {

    override val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        // TODO: implement
    }

    internal fun isColorValid(color: ColorHexPresentation): Boolean {

    }

    internal fun isColorValid(color: ColorRgbPresentation): Boolean {

    }
}