package io.github.mmolosay.thecolor.presentation.input.hsv

import androidx.compose.ui.graphics.Color
import android.graphics.Color as AndroidColor

/**
 * Color utils for the 'Visual Picker Color Input' feature.
 */
internal object ColorUtils {

    /**
     * The range of valid hue values in HSV color space.
     * Derives from the specification in the Android SDK.
     *
     * @see AndroidColor.colorToHSV
     * @see AndroidColor.HSVToColor
     */
    val HsvHueRange = 0f..<360f
    val HsvSaturationRange = 0f..1f
    val HsvValueRange = 0f..1f

    fun HsvColor(components: FloatArray): Color {
        require(components.size == 3)
        val (h, s, v) = components
        require(h in HsvHueRange)
        require(s in HsvSaturationRange)
        require(v in HsvValueRange)
        val argb = AndroidColor.HSVToColor(components)
        return Color(argb)
    }
}