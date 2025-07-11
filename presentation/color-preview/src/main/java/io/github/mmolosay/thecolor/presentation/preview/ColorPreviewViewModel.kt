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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import javax.inject.Named

/**
 * Handles presentation logic of the 'Color Preview' feature.
 *
 * Unlike typical `ViewModel`s, it doesn't derive from Google's [ViewModel][androidx.lifecycle.ViewModel],
 * thus cannot be instantiated using [ViewModelProvider][androidx.lifecycle.ViewModelProvider].
 *
 * Instead, it can be created within "simple" `ViewModel` or Google's `ViewModel`.
 */
class ColorPreviewViewModel @AssistedInject constructor(
    @Assisted coroutineScope: CoroutineScope,
    @Assisted colorFlow: Flow<Color?>,
    @Assisted colorProcessedConfirmation: SendChannel<Color?>?,
    private val colorToColorInt: ColorToColorIntUseCase,
    @Named("defaultDispatcher") private val defaultDispatcher: CoroutineDispatcher,
) : SimpleViewModel(coroutineScope) {

    val dataFlow: StateFlow<ColorPreviewData?> = kotlin.run {
        fun data(color: Color?) =
            ColorPreviewData(
                color = with(colorToColorInt) { color?.toColorInt() },
            )
        colorFlow
            .transform { color ->
                val data = data(color)
                emit(data)
                colorProcessedConfirmation?.send(color)
            }
            .flowOn(defaultDispatcher)
            .stateIn(coroutineScope, SharingStarted.Eagerly, initialValue = null)
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            coroutineScope: CoroutineScope,
            colorFlow: Flow<Color?>,
            colorProcessedConfirmation: SendChannel<Color?>?,
        ): ColorPreviewViewModel
    }
}