package io.github.mmolosay.thecolor.domain.color

import io.github.mmolosay.thecolor.domain.color.prototype.ColorPrototype
import io.github.mmolosay.thecolor.domain.color.prototype.ColorPrototypeValidator
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
        val hexInt = Random.nextInt(Color.Hex.Range)
        return Color.Hex(value = hexInt)
    }
}