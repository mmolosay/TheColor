package io.github.mmolosay.thecolor.presentation.devoptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.domain.buildfeatures.BuildInfoRepository
import io.github.mmolosay.thecolor.domain.dev.options.DefaultDevOptions
import io.github.mmolosay.thecolor.domain.dev.options.DevOptionsRepository
import io.github.mmolosay.thecolor.domain.utils.PrefState
import io.github.mmolosay.thecolor.domain.utils.getOrElse
import io.github.mmolosay.thecolor.main.di.qualifiers.CoroutineDispatcherDiQualifiers.DefaultDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import io.github.mmolosay.thecolor.domain.dev.options.DevOptions.HttpLogging as DomainHttpLogging
import io.github.mmolosay.thecolor.domain.dev.options.DevOptions.PredictableRandomColors as DomainPredictableRandomColors
import io.github.mmolosay.thecolor.domain.dev.options.DevOptions.StrictMode as DomainStrictMode

@HiltViewModel
class DevOptionsViewModel @Inject constructor(
    private val devOptionsRepository: DevOptionsRepository,
    private val defaultDevOptions: DefaultDevOptions,
    private val buildInfoRepository: BuildInfoRepository,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {

    val dataStateFlow: StateFlow<DataState> =
        combine(
            flows = listOf(
                devOptionsRepository.flowOfPredictableRandomColors,
                devOptionsRepository.flowOfStrictMode,
                devOptionsRepository.flowOfHttpLogging,
            ),
            transform = ::createData,
        )
            .map { data -> DataState.Ready(data) }
            .flowOn(defaultDispatcher)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = DataState.Loading,
            )

    private val buildInfo by lazy {
        DevOptionsData.BuildInfo(
            appBuildType = buildInfoRepository.getAppBuildType(),
            appVersionName = buildInfoRepository.getAppBuildVersionName(),
            appVersionCode = buildInfoRepository.getAppBuildVersionCode(),
        )
    }

    private fun clearRepository() {
        viewModelScope.launch(defaultDispatcher) {
            devOptionsRepository.clear()
        }
    }

    private fun updatePredictableRandomColors(value: DomainPredictableRandomColors) {
        viewModelScope.launch(defaultDispatcher) {
            devOptionsRepository.setPredictableRandomColors(value)
        }
    }

    private fun updateStrictModeEnablement(value: Boolean) {
        viewModelScope.launch(defaultDispatcher) {
            val domainModel = DomainStrictMode(enabled = value)
            devOptionsRepository.setStrictMode(domainModel)
        }
    }

    private fun updateHttpLoggingEnablement(value: Boolean) {
        viewModelScope.launch(defaultDispatcher) {
            val domainModel = DomainHttpLogging(enabled = value)
            devOptionsRepository.setHttpLogging(domainModel)
        }
    }

    // this is the only way to combine() more than 5 flows of different types
    private fun createData(
        devOptions: Array<PrefState<Any>>,
    ): DevOptionsData {
        val iterator = devOptions.iterator()
        fun <T> nextValue(default: T): T {
            @Suppress("UNCHECKED_CAST")
            val prefState = iterator.next() as PrefState<T>
            return prefState.getOrElse { default }
        }
        return createData(
            predictableRandomColors = nextValue(defaultDevOptions.predictableRandomColors),
            strictMode = nextValue(defaultDevOptions.strictMode),
            httpLogging = nextValue(defaultDevOptions.httpLogging),
        )
    }

    private fun createData(
        predictableRandomColors: DomainPredictableRandomColors,
        strictMode: DomainStrictMode,
        httpLogging: DomainHttpLogging,
    ): DevOptionsData {
        return DevOptionsData(
            resetValuesToDefault = ::clearRepository,

            predictableRandomColors = predictableRandomColors,
            predictableRandomColorsByDefault = defaultDevOptions.predictableRandomColors,
            changePredictableRandomColors = ::updatePredictableRandomColors,

            isStrictModeEnabled = strictMode.enabled,
            isStrictModeEnabledByDefault = defaultDevOptions.strictMode.enabled,
            changeStrictModeEnablement = ::updateStrictModeEnablement,

            isHttpLoggingEnabled = httpLogging.enabled,
            isHttpLoggingEnabledByDefault = defaultDevOptions.httpLogging.enabled,
            changeHttpLoggingEnablement = ::updateHttpLoggingEnablement,

            buildInfo = buildInfo,
        )
    }

    sealed interface DataState {
        data object Loading : DataState
        data class Ready(val data: DevOptionsData) : DataState
    }
}