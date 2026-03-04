package io.github.mmolosay.thecolor.presentation.input.hsv

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.usecase.ColorConverter
import io.github.mmolosay.thecolor.presentation.common.viewmodel.SimpleViewModel
import io.github.mmolosay.thecolor.presentation.input.ColorInputMediator
import io.github.mmolosay.thecolor.presentation.input.colorState
import io.github.mmolosay.thecolor.presentation.input.model.DataState
import io.github.mmolosay.thecolor.utils.Sampler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Named
import kotlin.time.Duration.Companion.milliseconds
import io.github.mmolosay.thecolor.domain.model.ColorInputType as DomainColorInputType

/**
 * Handles presentation logic of the 'HSV Color Input' feature.
 *
 * Unlike typical `ViewModel`s, it doesn't derive from Google's [ViewModel][androidx.lifecycle.ViewModel],
 * thus cannot be instantiated using [ViewModelProvider][androidx.lifecycle.ViewModelProvider].
 *
 * Instead, it can be created within "simple" `ViewModel` or Google's `ViewModel`.
 */
class ColorInputHsvViewModel @AssistedInject constructor(
    @Assisted coroutineScope: CoroutineScope,
    @Assisted private val mediator: ColorInputMediator,
    private val colorConverter: ColorConverter,
    @Named("defaultDispatcher") private val defaultDispatcher: CoroutineDispatcher,
) : SimpleViewModel(coroutineScope) {

    private val _dataStateFlow = MutableStateFlow<DataState<ColorInputHsvData>>(DataState.BeingInitialized)
    val dataStateFlow: StateFlow<DataState<ColorInputHsvData>> = _dataStateFlow.asStateFlow()

    private var sampleProcessingJob: Job? = null // 'onSampleProduced' is never invoked concurrently
    private val samplerForNewColors = Sampler<ColorWithId>(
        period = 200.milliseconds,
        coroutineScope = CoroutineScope(coroutineScope.coroutineContext + defaultDispatcher),
    ) { colorWithId ->
        sampleProcessingJob?.cancel()
        sampleProcessingJob = coroutineScope.launch(defaultDispatcher) {
            mediator.withLock { editor ->
                val idThen = colorWithId.mediatorStateId
                val idNow = mediator.colorState.id
                if (idNow == idThen) {
                    editor.set(color = colorWithId.color, source = DomainColorInputType.Hsv)
                }
            }
        }
    }

    init {
        coroutineScope.launch(defaultDispatcher) {
            mediator.colorStateFlow.collect { (color, source) ->
                val data = ColorInputHsvData(
                    color = with(colorConverter) { color?.toHsv() },
                    onColorChanged = ::onColorChanged,
                )
                _dataStateFlow.value = DataState.Ready(data)
            }
        }
    }

    private fun onColorChanged(newColor: Color.Hsv) {
        _dataStateFlow.update { dataState ->
            if (dataState !is DataState.Ready) return@update dataState
            val newData = dataState.data.copy(color = newColor)
            DataState.Ready(data = newData)
        }
        val colorWithId = ColorWithId(color = newColor, mediatorStateId = mediator.colorState.id)
        samplerForNewColors.offer(colorWithId)
    }

    /**
     * Couples [color] received via [onColorChanged] with [ColorInputMediator.ColorState.id]
     * at the moment when [color] was received.
     */
    private data class ColorWithId(
        val color: Color.Hsv,
        val mediatorStateId: Int,
    )

    @AssistedFactory
    fun interface Factory {
        fun create(
            coroutineScope: CoroutineScope,
            mediator: ColorInputMediator,
        ): ColorInputHsvViewModel
    }
}