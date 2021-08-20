package com.ordolabs.thecolor.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ordolabs.thecolor.util.ExceptionHandler
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {

    val coroutineExceptionMessageRes: LiveData<Int> get() = _coroutineExceptionMessageRes
    protected val _coroutineExceptionMessageRes = MutableLiveData(0)

    open val coroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
        val messageRes = ExceptionHandler.parseExceptionType(throwable)
        _coroutineExceptionMessageRes.value = messageRes
    }

    /**
     * Launches specified coroutine [block] and handles it with [coroutineExceptionHandler].
     */
    protected fun launchCoroutine(block: suspend CoroutineScope.() -> Unit): Job =
        viewModelScope.launch(coroutineExceptionHandler) { block() }
}