package com.ordolabs.thecolor.viewmodel

import com.ordolabs.domain.usecase.GetColorInfoBaseUseCase
import kotlinx.coroutines.CoroutineExceptionHandler

internal class HomeViewModel(
    private val getColorInfoUseCase: GetColorInfoBaseUseCase
) : BaseViewModel()