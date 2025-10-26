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

    fun random(): Color {
        val hexIntRange = HexColorIntRange
        val hexInt = Random.nextInt(hexIntRange)
        return Color.Hex(value = hexInt)
    }
}