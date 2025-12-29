package io.github.mmolosay.thecolor.presentation.input.picker

import androidx.compose.ui.graphics.Color
import android.graphics.Color as AndroidColor

/**
 * Color utils for the 'Visual Picker Color Input' feature.
 */
internal object ColorUtils {

    fun HsvColor(components: FloatArray): Color {
        require(components.size == 3)
        val (h, s, v) = components
        require(h in 0f..<360f)
        require(s in 0f..1f)
        require(v in 0f..1f)
        val argb = AndroidColor.HSVToColor(components)
        return Color(argb)
    }
}