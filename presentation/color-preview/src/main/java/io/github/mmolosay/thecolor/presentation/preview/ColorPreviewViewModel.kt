package io.github.mmolosay.thecolor.presentation.preview

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.presentation.ColorInputColorProvider
import io.github.mmolosay.thecolor.presentation.ColorToColorIntUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Not a ViewModel-ViewModel in terms of Android development.
 * It doesn't derive from [androidx.lifecycle.ViewModel], so should only be used in "real" ViewModels
 * which do derive from Android-aware implementation.
 */
class ColorPreviewViewModel @AssistedInject constructor(
    @Assisted coroutineScope: CoroutineScope,
    @Assisted colorInputColorProvider: ColorInputColorProvider,
    private val colorToColorInt: ColorToColorIntUseCase,
) {

    val stateFlow: StateFlow<ColorPreviewData> =
        colorInputColorProvider.colorFlow
            .map { color ->
                ColorPreviewData(
                    color = with(colorToColorInt) { color?.toColorInt() },
                )
            }
            .stateIn(
                scope = coroutineScope,
                started = SharingStarted.Eagerly,
                initialValue = ColorPreviewData(color = null),
            )

    @AssistedFactory
    interface Factory {
        fun create(
            coroutineScope: CoroutineScope,
            colorInputColorProvider: ColorInputColorProvider,
        ): ColorPreviewViewModel
    }
}