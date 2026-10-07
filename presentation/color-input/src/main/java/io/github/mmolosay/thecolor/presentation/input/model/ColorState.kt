package io.github.mmolosay.thecolor.presentation.input.model

import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorInputType

data class ColorState(
    val color: Color?,
    val source: ColorInputType?,
    val revision: Revision,
) {

    @JvmInline
    value class Revision(private val value: Long) : Comparable<Revision> {
        fun next() = Revision(this.value + 1)
        override fun compareTo(other: Revision): Int = this.value compareTo other.value
    }
}