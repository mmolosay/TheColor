package io.github.mmolosay.thecolor.presentation.devoptions.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.domain.dev.options.DefaultDevOptions
import io.github.mmolosay.thecolor.domain.dev.options.DevOptionsRepository
import io.github.mmolosay.thecolor.domain.utils.readyValues
import io.github.mmolosay.thecolor.domain.utils.valueOrNull
import io.github.mmolosay.thecolor.utils.mapState
import io.github.mmolosay.thecolor.utils.requireSend
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import io.github.mmolosay.thecolor.domain.dev.options.DevOptions.HttpLogging as DomainHttpLogging
import io.github.mmolosay.thecolor.domain.dev.options.DevOptions.PredictableRandomColors as DomainPredictableRandomColors
import io.github.mmolosay.thecolor.domain.dev.options.DevOptions.StrictMode as DomainStrictMode

@HiltViewModel
class DevOptionsViewModel @Inject constructor(
    coreFactory: DevOptionsCore.Factory,
    private val devOptionsRepository: DevOptionsRepository,
    private val defaultDevOptions: DefaultDevOptions,
) : ViewModel() {

    private val core = coreFactory.create()
    private val store = run {
        val state = DevOptionsState(
            predictableRandomColors = devOptionsRepository.flowOfPredictableRandomColors
                .value.valueOrNull(defaultDevOptions.predictableRandomColors),
            strictMode = devOptionsRepository.flowOfStrictMode
                .value.valueOrNull(defaultDevOptions.strictMode),
            httpLogging = devOptionsRepository.flowOfHttpLogging
                .value.valueOrNull(defaultDevOptions.httpLogging),
        )
        Store(state)
    }
    private val writes = Channel<suspend () -> Unit>(capacity = Channel.UNLIMITED)

    val viewData: StateFlow<DevOptionsViewData?> =
        store.mapState { state -> with(core) { state.toViewData() } }

    init {
        collectDevOptions()
        viewModelScope.launch {
            for (write in writes) write()
        }
    }

    private fun collectDevOptions() {
        viewModelScope.launch {
            launch {
                devOptionsRepository.flowOfPredictableRandomColors
                    .readyValues(defaultDevOptions.predictableRandomColors)
                    .collect { value ->
                        store.update { state ->
                            with(core) { state.applyPredictableRandomColors(value) }
                        }
                    }
            }
            launch {
                devOptionsRepository.flowOfStrictMode
                    .readyValues(defaultDevOptions.strictMode)
                    .collect { value ->
                        store.update { state ->
                            with(core) { state.applyStrictMode(value) }
                        }
                    }
            }
            launch {
                devOptionsRepository.flowOfHttpLogging
                    .readyValues(defaultDevOptions.httpLogging)
                    .collect { value ->
                        store.update { state ->
                            with(core) { state.applyHttpLogging(value) }
                        }
                    }
            }
        }
    }

    fun changePredictableRandomColors(value: DomainPredictableRandomColors) {
        writes.requireSend {
            devOptionsRepository.setPredictableRandomColors(value)
        }
    }

    fun changeStrictMode(enabled: Boolean) {
        writes.requireSend {
            devOptionsRepository.setStrictMode(DomainStrictMode(enabled))
        }
    }

    fun changeHttpLogging(enabled: Boolean) {
        writes.requireSend {
            devOptionsRepository.setHttpLogging(DomainHttpLogging(enabled))
        }
    }

    fun resetToDefaults() {
        writes.requireSend {
            devOptionsRepository.clear()
        }
    }
}