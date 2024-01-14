package io.github.mmolosay.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
        launchIn(Dispatchers.Main.immediate) {
            _coroutineExceptionMessageRes.emit(messageRes)
        }
    }

    /**
     * [SharedFlow] that will emit @StringRes ids, corresponding to occured while
     * coroutine execution with [launchIn].
     *
     * [coroutineExceptionMessageRes] can be used to get its String resource and
     * show it on UI as `Snackbar` or `Toast`.
     *
     * @see launchIn
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
    protected fun BaseViewModel.launchIn(
        dispatcher: CoroutineDispatcher,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        val context = dispatcher + this.coroutineExceptionHandler
        return this.viewModelScope.launch(context = context, block = block)
    }

    /**
     * Launches specified coroutine [block] in [Dispatchers.Main] dispatcher.
     *
     * @see BaseViewModel.launchIn
     */
    @Suppress("unused")
    protected fun BaseViewModel.launchInMain(
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        return this.launchIn(Dispatchers.Main, block)
    }

    /**
     * Launches specified coroutine [block] in [Dispatchers.IO] dispatcher.
     *
     * @see BaseViewModel.launchIn
     */
    @Suppress("unused")
    protected fun BaseViewModel.launchInIO(
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        return this.launchIn(Dispatchers.IO, block)
    }

    /**
     * Launches specified coroutine [block] on [coroutineDispatcherDefault].
     *
     * @see BaseViewModel.launchIn
     */
    @Suppress("unused")
    protected fun BaseViewModel.launch(
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        return this.launchIn(this.coroutineDispatcherDefault, block)
    }
}