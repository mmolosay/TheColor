package com.ordolabs.thecolor.viewmodel

import kotlinx.coroutines.CoroutineExceptionHandler

class ColorInputViewModel : BaseViewModel() {

    override val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        // TODO: implement
    }
}