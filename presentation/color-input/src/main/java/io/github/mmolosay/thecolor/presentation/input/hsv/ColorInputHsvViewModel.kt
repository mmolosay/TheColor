package io.github.mmolosay.thecolor.presentation.input.hsv

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorConverter
import io.github.mmolosay.thecolor.main.di.qualifiers.CoroutineDispatcherDiQualifiers.DefaultDispatcher
import io.github.mmolosay.thecolor.presentation.common.viewmodel.SimpleViewModel
import io.github.mmolosay.thecolor.presentation.common.viewmodel.Store
import io.github.mmolosay.thecolor.presentation.input.ColorInputMediator
import io.github.mmolosay.thecolor.presentation.input.colorState
import io.github.mmolosay.thecolor.utils.Sampler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
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

    private val orderedUpdates = defaultDispatcher.limitedParallelism(1)

    val dataFlow: StateFlow<ColorInputHsvData> =
        store.flow.stateIn(coroutineScope, SharingStarted.Lazily, store.value)

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

    fun facade(data: ColorInputHsvData): ColorInputHsvFacade =
        ColorInputHsvFacadeImpl(
            data = data,
            viewModel = this,
        )

    fun setColor(newColor: Color.Hsv) {
        coroutineScope.launch(orderedUpdates) {
            store.update {
                it.copy(color = newColor)
            }
            val colorWithId = ColorWithId(color = newColor, mediatorStateId = mediator.colorState.id)
            samplerForNewColors.offer(colorWithId)
        }
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

private class ColorInputHsvFacadeImpl(
    private val data: ColorInputHsvData,
    private val viewModel: ColorInputHsvViewModel,
) : ColorInputHsvFacade {

    override val color = data.color

    override fun setColor(color: Color.Hsv) =
        viewModel.setColor(color)

    override fun equals(other: Any?): Boolean =
        other is ColorInputHsvFacadeImpl && this.data == other.data && this.viewModel === other.viewModel

    override fun hashCode(): Int {
        var result = data.hashCode()
        result = 31 * result + System.identityHashCode(viewModel)
        return result
    }
}