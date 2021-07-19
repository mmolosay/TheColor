package com.ordolabs.thecolor.viewmodel

import com.ordolabs.domain.usecase.GetColorInfoBaseUseCase
import kotlinx.coroutines.CoroutineExceptionHandler

class HomeViewModel(
    private val getColorInfoUseCase: GetColorInfoBaseUseCase
) : BaseViewModel() {

    override val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        // fill me
    }


}