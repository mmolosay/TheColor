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
import io.github.mmolosay.thecolor.utils.Store
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
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
    @Assisted private val store: Store<ColorInputHsvData>,
    @Assisted private val mediator: ColorInputMediator,
    private val colorConverter: ColorConverter,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : SimpleViewModel(coroutineScope) {

    private val exclusiveLane = defaultDispatcher.limitedParallelism(1)

    val dataFlow: StateFlow<ColorInputHsvData> = store.flow

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
                    editor.set(
                        color = colorWithId.color,
                        source = ColorInputSource(DomainColorInputType.Hsv),
                    )
                }
            }
        }
    }

    init {
        collectMediatorUpdates()
    }

    private fun collectMediatorUpdates() {
        coroutineScope.launch(defaultDispatcher) {
            mediator.colorStateFlow.collect { colorState ->
                val color = with(colorConverter) { colorState.color?.toHsv() }
                store.update {
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

    private suspend fun setColor(newColor: Color.Hsv) {
        store.update {
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
            store: Store<ColorInputHsvData>,
            mediator: ColorInputMediator,
        ): ColorInputHsvViewModel
    }
}

class ColorInputHsvDataFactory @Inject constructor(
    private val mediator: ColorInputMediator,
    private val colorConverter: ColorConverter,
) {
    fun create(
        color: Color.Hsv? = colorFromMediator(),
    ) =
        ColorInputHsvData(
            color = color,
        )

    fun colorFromMediator(): Color.Hsv? =
        with(colorConverter) { mediator.colorState.color?.toHsv() }
}