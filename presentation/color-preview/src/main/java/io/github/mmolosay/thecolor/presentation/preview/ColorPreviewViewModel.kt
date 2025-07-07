package io.github.mmolosay.thecolor.presentation.preview

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.presentation.api.ColorToColorIntUseCase
import io.github.mmolosay.thecolor.presentation.api.SimpleViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
    @Assisted colorFlow: StateFlow<Color?>,
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
            .map(::value)
            .flowOn(defaultDispatcher)
            .stateIn(coroutineScope, SharingStarted.Eagerly, initialValue)
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            coroutineScope: CoroutineScope,
            colorFlow: StateFlow<Color?>,
        ): ColorPreviewViewModel
    }
}