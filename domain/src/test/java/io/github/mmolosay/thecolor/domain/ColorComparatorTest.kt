package io.github.mmolosay.thecolor.domain

import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorComparator
import io.github.mmolosay.thecolor.domain.color.ColorConverter
import io.github.mmolosay.thecolor.domain.color.isSameAs
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ColorComparatorTest {

    val sut = ColorComparator(
        colorConverter = ColorConverter(),
    )

    @Test
    fun `when 'null' color is compared with 'null' color, then they're the same`() {
        val noColor: Color? = null

        val isSame = context(sut) { noColor isSameAs noColor }

        isSame shouldBe true
    }

    @Test
    fun `when 'null' color is compared with some color, then they're NOT the same`() {
        val noColor: Color? = null

        val isSame = context(sut) { noColor isSameAs Color.Hex(0xFF0000) }

        isSame shouldBe false
    }

    @Test
    fun `when red in HEX is compared with red in RGB, then they're the same`() {
        val hex = Color.Hex(0xFF0000)
        val rgb = Color.Rgb(r = 255, g = 0, b = 0)

        val isSame = context(sut) { hex isSameAs rgb }

        isSame shouldBe true
    }
}
