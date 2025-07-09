package io.github.mmolosay.thecolor.presentation.preview

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.presentation.api.ColorToColorIntUseCase
import io.github.mmolosay.thecolor.presentation.api.SimpleViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import timber.log.Timber
import javax.inject.Named

/**
 * Handles presentation logic of the 'Color Preview' feature.
 *
 * Unlike typical `ViewModel`s, it doesn't derive from Google's [ViewModel][androidx.lifecycle.ViewModel],
 * thus cannot be instantiated using [ViewModelProvider][androidx.lifecycle.ViewModelProvider].
 *
 * Instead, it can be created within "simple" `ViewModel` or Google's `ViewModel`.
 */
// TODO: remove logs
class ColorPreviewViewModel @AssistedInject constructor(
    @Assisted coroutineScope: CoroutineScope,
    @Assisted colorFlow: StateFlow<Color?>,
    @Assisted colorProcessedConfirmation: SendChannel<Color?>?,
    private val colorToColorInt: ColorToColorIntUseCase,
    @Named("defaultDispatcher") private val defaultDispatcher: CoroutineDispatcher,
) : SimpleViewModel(coroutineScope) {

    val dataFlow: StateFlow<ColorPreviewData> = kotlin.run {
        fun value(color: Color?) =
            ColorPreviewData(
                color = with(colorToColorInt) { color?.toColorInt() },
            )

        val initialValue = value(colorFlow.value)
        colorFlow
            .transform { color ->
//                Timber.d("HomeViewModel | ColorPreviewViewModel dataFlow, new color $color, delay starts")
//                delay(100) // TODO: remove me
//                Timber.d("HomeViewModel | ColorPreviewViewModel dataFlow, delay finished")
                emit(value(color))
                Timber.d("HomeViewModel | ColorPreviewViewModel dataFlow, data for color $color emitted")
                colorProcessedConfirmation?.send(color)
                Timber.d("HomeViewModel | ColorPreviewViewModel dataFlow, $color processed confirmation was sent")
                println() // TODO: breakpoint, remove me
            }
            .flowOn(defaultDispatcher)
            .stateIn(coroutineScope, SharingStarted.Eagerly, initialValue)
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            coroutineScope: CoroutineScope,
            colorFlow: StateFlow<Color?>,
            colorProcessedConfirmation: SendChannel<Color?>?,
        ): ColorPreviewViewModel
    }
}