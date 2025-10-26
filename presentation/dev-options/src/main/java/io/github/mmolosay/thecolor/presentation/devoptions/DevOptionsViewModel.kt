package io.github.mmolosay.thecolor.presentation.devoptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.domain.repository.DefaultDevOptions
import io.github.mmolosay.thecolor.domain.repository.DevOptionsRepository
import io.github.mmolosay.thecolor.domain.usecase.ResetDevOptionsToDefaultUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named
import io.github.mmolosay.thecolor.domain.model.DevOptions.PredictableRandomColors as DomainPredictableRandomColors

@HiltViewModel
class DevOptionsViewModel @Inject constructor(
    private val devOptionsRepository: DevOptionsRepository,
    private val resetDevOptionsToDefault: ResetDevOptionsToDefaultUseCase,
    @Named("defaultDispatcher") private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {

    val dataStateFlow: StateFlow<DataState> =
        combine(
            flows = listOf(
                devOptionsRepository.flowOfPredictableRandomColors,
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

    private fun resetValuesToDefault() {
        viewModelScope.launch(defaultDispatcher) {
            resetDevOptionsToDefault()
        }
    }

    private fun updatePredictableRandomColors(value: DomainPredictableRandomColors) {
        viewModelScope.launch(defaultDispatcher) {
            devOptionsRepository.setPredictableRandomColors(value)
        }
    }

    // that's the only way to combine() more than 5 flows of different types
    private fun createData(
        devOptions: Array<Any>,
    ): DevOptionsData {
        val iterator = devOptions.iterator()
        return createData(
            predictableRandomColors = iterator.next() as DomainPredictableRandomColors,
        )
    }

    private fun createData(
        predictableRandomColors: DomainPredictableRandomColors,
    ): DevOptionsData {
        return DevOptionsData(
            resetValuesToDefault = ::resetValuesToDefault,

            predictableRandomColors = predictableRandomColors,
            defaultPredictableRandomColors = DefaultDevOptions.PredictableRandomColors,
            changePredictableRandomColors = ::updatePredictableRandomColors,
        )
    }

    sealed interface DataState {
        data object Loading : DataState
        data class Ready(val data: DevOptionsData) : DataState
    }
}