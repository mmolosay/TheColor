package io.github.mmolosay.thecolor.domain.color

import io.github.mmolosay.thecolor.domain.dev.options.DefaultDevOptions
import io.github.mmolosay.thecolor.domain.dev.options.DevOptions.PredictableRandomColors
import io.github.mmolosay.thecolor.domain.dev.options.DevOptionsRepository
import io.github.mmolosay.thecolor.utils.getOrElse
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Produces a random [Color].
 * This use case accounts for "predictable random colors" feature from "Developer Options".
 */
@Singleton // retain the same instance to accurately keep track of the next color for "cycling" strategies
class GetPredictableRandomColorUseCase @Inject constructor(
    private val colorFactory: ColorFactory,
    private val devOptionsRepository: DevOptionsRepository,
    private val defaultDevOptions: DefaultDevOptions,
) {

    private val cyclingRgb by lazy {
        CyclicColorsFactory(
            colors = listOf(
                Color.Hex(0xFF0000),
                Color.Hex(0x00FF00),
                Color.Hex(0x0000FF),
            ),
        )
    }
    private val cyclingLightDark by lazy {
        CyclicColorsFactory(
            colors = listOf(
                Color.Hex(0xEEEEEE),
                Color.Hex(0x111111),
            ),
        )
    }

    operator fun invoke(): Color {
        val strategy = devOptionsRepository.flowOfPredictableRandomColors
            .value.getOrElse { defaultDevOptions.predictableRandomColors }
        return when (strategy) {
            PredictableRandomColors.Random -> colorFactory.random()
            PredictableRandomColors.CyclingRgb -> cyclingRgb.next()
            PredictableRandomColors.CyclingLightDark -> cyclingLightDark.next()
        }
    }
}

private class CyclicColorsFactory(
    private val colors: List<Color>,
) {

    private var indexOfNextColor = 0

    @Synchronized
    fun next(): Color {
        val color = colors[indexOfNextColor]
        indexOfNextColor++
        indexOfNextColor %= colors.size
        return color
    }
}