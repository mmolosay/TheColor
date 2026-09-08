package io.github.mmolosay.thecolor.presentation.preview

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.presentation.common.colorint.ColorToColorIntUseCase
import io.github.mmolosay.thecolor.presentation.common.viewmodel.SimpleViewModel
import io.github.mmolosay.thecolor.utils.Store
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

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
    @Assisted private val store: Store<ColorPreviewData>,
    private val colorToColorInt: ColorToColorIntUseCase,
) : SimpleViewModel(coroutineScope) {

    val dataFlow: StateFlow<ColorPreviewData> = store.flow

    /**
     * Sets the new [color].
     * It will be transformed to the [ColorPreviewData] and exposed via [dataFlow].
     */
    suspend fun setColor(color: Color?) {
        val colorInt = with(colorToColorInt) { color?.toColorInt() }
        store.update {
            it.copy(color = colorInt)
        }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            coroutineScope: CoroutineScope,
            store: Store<ColorPreviewData>,
        ): ColorPreviewViewModel
    }
}

class ColorPreviewDataFactory @Inject constructor(
    private val colorToColorInt: ColorToColorIntUseCase,
) {
    fun create(
        color: Color?,
    ): ColorPreviewData =
        ColorPreviewData(
            color = with(colorToColorInt) { color?.toColorInt() },
        )
}