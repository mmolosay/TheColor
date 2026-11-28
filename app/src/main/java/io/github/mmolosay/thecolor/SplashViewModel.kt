package io.github.mmolosay.thecolor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.domain.TouchLocalDatabaseUseCase
import io.github.mmolosay.thecolor.domain.color.LastSearchedColorRepository
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferencesRepository
import io.github.mmolosay.thecolor.domain.utils.filterReady
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val touchLocalDatabase: TouchLocalDatabaseUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val lastSearchedColorRepository: LastSearchedColorRepository,
) : ViewModel() {

    private val _isWorkCompleteFlow = MutableStateFlow(false)
    val isWorkCompleteFlow: StateFlow<Boolean> = _isWorkCompleteFlow.asStateFlow()

    init {
        doWork()
    }

    private fun doWork() {
        viewModelScope.launch {
            coroutineScope {
                launch {
                    touchLocalDatabase()
                }
                launch {
                    // the start screen (Home) reads these at creation, so the splash waits for them
                    userPreferencesRepository.flowOfResumeFromLastSearchedColorOnStartup
                        .filterReady().first()
                    lastSearchedColorRepository.flowOfLastSearchedColor
                        .filterReady().first()
                    userPreferencesRepository.flowOfAutoProceedWithRandomizedColors
                        .filterReady().first()
                    userPreferencesRepository.flowOfColorInputType
                        .filterReady().first()
                }
            }
            // will wait for the coroutineScope() above to finish
            _isWorkCompleteFlow.value = true
        }
    }
}