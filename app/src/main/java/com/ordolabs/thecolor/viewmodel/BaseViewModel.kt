package com.ordolabs.thecolor.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ordolabs.thecolor.util.ExceptionHandler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {

    /**
     * Specifies default [CoroutineDispatcher] for `this ViewModel`.
     */
    protected open val coroutineDispatcherDefault: CoroutineDispatcher = Dispatchers.Default

    /**
     * Specifies [CoroutineExceptionHandler] to be used in `this ViewModel` [CoroutineContext].
     */
    protected open val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        val messageRes = ExceptionHandler.parseExceptionType(throwable)
        _coroutineExceptionMessageRes.value = messageRes
    }

    val coroutineExceptionMessageRes: LiveData<Int> get() = _coroutineExceptionMessageRes
    protected val _coroutineExceptionMessageRes = MutableLiveData(0)

    /**
     * Launches specified coroutine [block] in current [viewModelScope] on [dispatcher].
     *
     * All exceptions are being handled with [coroutineExceptionHandler].
     *
     * @param dispatcher [CoroutineDispatcher] for specified `suspend` [block].
     * @param block the body of coroutine.
     */
    protected fun BaseViewModel.launchOn(
        dispatcher: CoroutineDispatcher,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        val context = dispatcher + this.coroutineExceptionHandler
        return this.viewModelScope.launch(context = context, block = block)
    }

    /**
     * Launches specified coroutine [block] on [BaseViewModel.coroutineDispatcherDefault].
     *
     * @see BaseViewModel.launchOn
     */
    protected fun BaseViewModel.launch(
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        return this.launchOn(this.coroutineDispatcherDefault, block)
    }
}