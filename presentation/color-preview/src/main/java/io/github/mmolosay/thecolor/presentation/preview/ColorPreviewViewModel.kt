package io.github.mmolosay.thecolor.presentation.preview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.presentation.ColorInputColorProvider
import io.github.mmolosay.thecolor.presentation.ColorToColorIntUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ColorPreviewViewModel @Inject constructor(
    colorInputColorProvider: ColorInputColorProvider,
    private val colorToColorInt: ColorToColorIntUseCase,
) : ViewModel() {

    val stateFlow: StateFlow<ColorPreviewData> =
        colorInputColorProvider.colorFlow
            .map { color ->
                ColorPreviewData(
                    color = with(colorToColorInt) { color?.toColorInt() },
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = ColorPreviewData(color = null),
            )
}