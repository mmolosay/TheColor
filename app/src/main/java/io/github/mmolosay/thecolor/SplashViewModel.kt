package io.github.mmolosay.thecolor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.domain.usecase.TouchLocalDatabaseUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val touchLocalDatabase: TouchLocalDatabaseUseCase,
    @Named("defaultDispatcher") private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _isWorkCompleteFlow = MutableStateFlow(false)
    val isWorkCompleteFlow: StateFlow<Boolean> = _isWorkCompleteFlow.asStateFlow()

    init {
        doWork()
    }

    private fun doWork() {
        viewModelScope.launch(defaultDispatcher) {
            // run independent operations concurrently, each in individual coroutine
            coroutineScope {
                launch {
                    touchLocalDatabase()
                }
            }
            // will wait for the coroutineScope() above to finish
            _isWorkCompleteFlow.value = true
        }
    }
}