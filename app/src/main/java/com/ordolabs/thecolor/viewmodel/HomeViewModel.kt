package com.ordolabs.thecolor.viewmodel

import kotlinx.coroutines.CoroutineExceptionHandler

class HomeViewModel : BaseViewModel() {

    override val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        // fill me
    }
}