package io.github.mmolosay.thecolor.presentation.details

import androidx.compose.ui.graphics.Color
import io.github.mmolosay.thecolor.presentation.design.ColorsOnTintedSurface
import io.github.mmolosay.thecolor.presentation.design.colorsOnDarkSurface
import io.github.mmolosay.thecolor.presentation.design.colorsOnLightSurface
import io.github.mmolosay.thecolor.presentation.impl.toCompose

/**
 * Same approach as [ButtonDefaults][androidx.compose.material3.ButtonDefaults].
 */
object ColorDetailsOnTintedSurfaceDefaults {

    fun surfaceColor(seedData: ColorDetailsSeedData): Color =
        seedData.color.toCompose()

    fun colorsOnTintedSurface(seedData: ColorDetailsSeedData): ColorsOnTintedSurface {
        val useLight = seedData.isDark
        return if (useLight) colorsOnDarkSurface() else colorsOnLightSurface()
    }
}