package io.github.mmolosay.thecolor.presentation.input.hsv

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.presentation.common.viewmodel.SimpleViewModel
import io.github.mmolosay.thecolor.utils.Atom
import io.github.mmolosay.thecolor.utils.Sampler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.time.Duration.Companion.milliseconds

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
    @Assisted private val atom: Atom<ColorInputHsvState>,
) : SimpleViewModel(coroutineScope) {

    private val samplerForNewColors = Sampler<Color.Hsv>(
        period = 200.milliseconds,
        coroutineScope = coroutineScope,
    ) { sample ->
        // skipped if the pickers have moved on or the color was set elsewhere meanwhile
        atom.update {
            val hasChanged = (it.displayColor != sample)
            if (hasChanged) return@update it
            it.copy(color = sample)
        }
    }

    fun execute(action: ColorInputHsvAction): Job? =
        when (action) {
            is ColorInputHsvAction.SetColor -> {
                setColor(action.color)
                null
            }
        }

    private fun setColor(newColor: Color.Hsv) {
        atom.update {
            it.copy(displayColor = newColor)
        }
        samplerForNewColors.offer(newColor)
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            coroutineScope: CoroutineScope,
            atom: Atom<ColorInputHsvState>,
        ): ColorInputHsvViewModel
    }
}