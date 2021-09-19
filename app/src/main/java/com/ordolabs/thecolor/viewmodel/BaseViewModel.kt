package com.ordolabs.thecolor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ordolabs.thecolor.util.ExceptionHandler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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
        launchOn(Dispatchers.Main.immediate) {
            _coroutineExceptionMessageRes.emit(messageRes)
        }
    }

    /**
     * [SharedFlow] that will emit @StringRes ids, corresponding to occured while
     * coroutine execution with [launchOn].
     *
     * [coroutineExceptionMessageRes] can be used to get its String resource and
     * show it on UI as `Snackbar` or `Toast`.
     *
     * @see launchOn
     * @see coroutineExceptionHandler
     */
    protected val _coroutineExceptionMessageRes = MutableSharedFlow<Int>()
    val coroutineExceptionMessageRes = _coroutineExceptionMessageRes.asSharedFlow()

    /**
     * Launches specified coroutine [block] in current [viewModelScope] on [dispatcher].
     *
     * All exceptions are being handled with [coroutineExceptionHandler].
     *
     * @param dispatcher [CoroutineDispatcher] for specified `suspend` [block].
     * @param block the body of coroutine.
     */
    @Suppress("unused")
    protected fun BaseViewModel.launchOn(
        dispatcher: CoroutineDispatcher,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        val context = dispatcher + this.coroutineExceptionHandler
        return this.viewModelScope.launch(context = context, block = block)
    }

    /**
     * Launches specified coroutine [block] on [coroutineDispatcherDefault].
     *
     * @see BaseViewModel.launchOn
     */
    @Suppress("unused")
    protected fun BaseViewModel.launch(
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        return this.launchOn(this.coroutineDispatcherDefault, block)
    }
}