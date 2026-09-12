package io.github.mmolosay.thecolor.presentation.input.hsv

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorConverter
import io.github.mmolosay.thecolor.main.di.qualifiers.CoroutineDispatcherDiQualifiers.DefaultDispatcher
import io.github.mmolosay.thecolor.presentation.common.viewmodel.SimpleViewModel
import io.github.mmolosay.thecolor.presentation.input.ColorInputMediator
import io.github.mmolosay.thecolor.presentation.input.ColorInputSource
import io.github.mmolosay.thecolor.presentation.input.colorState
import io.github.mmolosay.thecolor.utils.Sampler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import io.github.mmolosay.thecolor.domain.color.ColorInputType as DomainColorInputType

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
    @Assisted private val _dataFlow: MutableStateFlow<ColorInputHsvData>,
    @Assisted private val mediator: ColorInputMediator,
    private val colorConverter: ColorConverter,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : SimpleViewModel(coroutineScope) {

    private val exclusiveLane = defaultDispatcher.limitedParallelism(1)

    val dataFlow: StateFlow<ColorInputHsvData> = _dataFlow.asStateFlow()

    private var sampleProcessingJob: Job? = null // 'onSampleProduced' is never invoked concurrently
    private val samplerForNewColors = Sampler<ColorWithId>(
        period = 200.milliseconds,
        coroutineScope = CoroutineScope(coroutineScope.coroutineContext + defaultDispatcher),
    ) { colorWithId ->
        sampleProcessingJob?.cancel()
        coroutineScope.launch(defaultDispatcher, CoroutineStart.UNDISPATCHED) {
            mediator.withLock { editor ->
                val state = mediator.colorState
                val source = state.source
                val isOwnChange = (source is ColorInputSource && source.type == DomainColorInputType.Hsv)
                val hasIdChanged = (state.id != colorWithId.mediatorStateId)
                if (hasIdChanged && !isOwnChange) return@withLock
                editor.set(
                    color = colorWithId.color,
                    source = ColorInputSource(DomainColorInputType.Hsv),
                )
            }
        }.also { sampleProcessingJob = it }
    }

    init {
        collectMediatorUpdates()
    }

    private fun collectMediatorUpdates() {
        coroutineScope.launch(defaultDispatcher) {
            mediator.colorStateFlow.collect { (color, source) ->
                // don't update color to avoid overwriting a newer one if the color was set from this 'Color Input' type
                if (source is ColorInputSource && source.type == DomainColorInputType.Hsv) return@collect
                val color = with(colorConverter) { color?.toHsv() }
                _dataFlow.update {
                    it.copy(color = color)
                }
            }
        }
    }

    fun execute(action: ColorInputHsvAction): Job =
        coroutineScope.launch(exclusiveLane) {
            when (action) {
                is ColorInputHsvAction.SetColor -> {
                    setColor(action.color)
                }
            }
        }

    private fun setColor(newColor: Color.Hsv) {
        _dataFlow.update {
            it.copy(color = newColor)
        }
        val colorWithId = ColorWithId(color = newColor, mediatorStateId = mediator.colorState.id)
        samplerForNewColors.offer(colorWithId)
    }

    /**
     * Couples [color] received via [setColor] with [ColorInputMediator.ColorState.id]
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
            dataFlow: MutableStateFlow<ColorInputHsvData>,
            mediator: ColorInputMediator,
        ): ColorInputHsvViewModel
    }
}

class ColorInputHsvDataFactory @Inject constructor(
    private val mediator: ColorInputMediator,
    private val colorConverter: ColorConverter,
) {

    fun create(
        color: Color.Hsv? = colorFromMediator(mediator),
    ): ColorInputHsvData =
        ColorInputHsvData(
            color = color,
        )

    fun colorFromMediator(mediator: ColorInputMediator): Color.Hsv? =
        with(colorConverter) { mediator.colorState.color?.toHsv() }
}