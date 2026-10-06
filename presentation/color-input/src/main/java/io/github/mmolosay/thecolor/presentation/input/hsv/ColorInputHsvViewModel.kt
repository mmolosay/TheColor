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
import io.github.mmolosay.thecolor.presentation.input.set
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
    @Assisted private val mediator: ColorInputMediator,
    dataFactory: ColorInputHsvDataFactory,
    private val colorConverter: ColorConverter,
    @DefaultDispatcher defaultDispatcher: CoroutineDispatcher,
) : SimpleViewModel(coroutineScope) {

    private val exclusiveLane = defaultDispatcher.limitedParallelism(1)

    private val _dataFlow = MutableStateFlow(dataFactory.create())
    val dataFlow: StateFlow<ColorInputHsvData> = _dataFlow.asStateFlow()

    private var sampleProcessingJob: Job? = null // 'onSampleProduced' is never invoked concurrently
    private val samplerForNewColors = Sampler<ColorWithRevision>(
        period = 200.milliseconds,
        coroutineScope = coroutineScope,
    ) { (color, revision) ->
        sampleProcessingJob?.cancel()
        coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
            // ignored by the mediator if a later change has been made meanwhile
            mediator.set(
                color = color,
                source = ColorInputSource(DomainColorInputType.Hsv),
                revision = revision,
            )
        }.also { sampleProcessingJob = it }
    }

    init {
        collectMediatorUpdates()
    }

    private fun collectMediatorUpdates() {
        coroutineScope.launch {
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
        val colorWithRevision = ColorWithRevision(color = newColor, revision = mediator.newRevision())
        samplerForNewColors.offer(colorWithRevision)
    }

    /**
     * Couples [color] received via [setColor] with the [ColorInputMediator.ColorState.Revision] of that change.
     */
    private data class ColorWithRevision(
        val color: Color.Hsv,
        val revision: ColorInputMediator.ColorState.Revision,
    )

    @AssistedFactory
    fun interface Factory {
        fun create(
            coroutineScope: CoroutineScope,
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