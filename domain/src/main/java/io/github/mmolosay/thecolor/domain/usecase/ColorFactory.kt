package io.github.mmolosay.thecolor.domain.usecase

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.model.ColorConstants.HexColorIntRange
import io.github.mmolosay.thecolor.domain.model.ColorPrototype
import javax.inject.Inject
import kotlin.random.Random
import kotlin.random.nextInt

/**
 * Creates instances of [Color].
 */
class ColorFactory @Inject constructor(
    private val colorPrototypeValidator: ColorPrototypeValidator,
) {

    private val randomColorFactory: RandomColorFactory = FullRangeColorFactory()

    fun from(prototype: ColorPrototype): Color? =
        when (prototype) {
            is ColorPrototype.Hex -> from(prototype)
            is ColorPrototype.Rgb -> from(prototype)
        }

    fun from(prototype: ColorPrototype.Hex): Color.Hex? {
        val valid = with(colorPrototypeValidator) { prototype.isValid() }
        if (valid.not()) return null
        return Color.Hex(value = prototype.value!!)
    }

    fun from(prototype: ColorPrototype.Rgb): Color.Rgb? {
        val valid = with(colorPrototypeValidator) { prototype.isValid() }
        if (valid.not()) return null
        return Color.Rgb(
            r = prototype.r!!,
            g = prototype.g!!,
            b = prototype.b!!,
        )
    }

    fun random(): Color =
        randomColorFactory.random()
}

private fun interface RandomColorFactory {
    fun random(): Color
}

private class FullRangeColorFactory : RandomColorFactory {

    override fun random(): Color {
        val hexIntRange = HexColorIntRange
        val hexInt = Random.nextInt(hexIntRange)
        return Color.Hex(value = hexInt)
    }
}

// TODO: sometimes it's useful to get a certain expected color from `ColorFactory.random()`.
//  For this purpose, an interface `RandomColorFactory` was defined. The default impl is a production behaviour.
//  If for some testing a more expected strategy of producing "random" colors is required, then `CyclicColorFactory` can be used.
//  In future, there will be 'Developer options' menu in the app, with a toggle to use either impl of `RandomColorFactory`.

private class CyclicColorFactory(
    private val colors: List<Color> = CyclicColors.RedGreenBlue,
) : RandomColorFactory {

    private var indexOfNextColor = 0

    @Synchronized
    override fun random(): Color {
        val color = colors[indexOfNextColor]
        indexOfNextColor++
        indexOfNextColor = indexOfNextColor % colors.size
        return color
    }
}

private object CyclicColors {

    val RedGreenBlue = listOf(
        Color.Hex(0xFF0000),
        Color.Hex(0x00FF00),
        Color.Hex(0x0000FF),
    )
    val DarkLight = listOf(
        Color.Hex(0xEEEEEE),
        Color.Hex(0x111111),
    )
}