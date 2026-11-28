package io.github.mmolosay.thecolor.presentation.input

import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorComparator
import io.github.mmolosay.thecolor.domain.color.ColorConverter
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvCore
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvState
import io.github.mmolosay.thecolor.utils.composition.Change
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class ColorInputHsvCoreTest {

    // real implementations: they're pure, and HSV's colors come from them
    val colorConverter = ColorConverter()

    val sut = ColorInputHsvCore(
        colorConverter = colorConverter,
        colorComparator = ColorComparator(colorConverter),
    )

    @Test
    fun `given no color, when 'pick' is invoked, then the change applies to the default position`() {
        with(sut) {
            val state = initialState(color = null)

            val picked = state.pick { current -> current.copy(hue = 240f) }

            val pickedState = picked.state.shouldBeInstanceOf<ColorInputHsvState.Picked>()
            pickedState.picked shouldBe Color.Hsv(hue = 240f, saturation = 1f, value = 1f)
        }
    }

    @Test
    fun `given a color was set from elsewhere, when 'pick' is invoked, then the change applies to that color`() {
        with(sut) {
            val state = initialState(Color.Hsv(hue = 120f, saturation = 0.5f, value = 0.25f))
                .pick { current -> current.copy(hue = 10f) }.state
                .withColor(Color.Hsv(hue = 200f, saturation = 0.2f, value = 0.9f)) // over the pick

            val picked = state.pick { current -> current.copy(hue = 240f) }

            val pickedState = picked.state.shouldBeInstanceOf<ColorInputHsvState.Picked>()
            pickedState.picked shouldBe Color.Hsv(hue = 240f, saturation = 0.2f, value = 0.9f)
        }
    }

    @Test
    fun `given 'Picked', when 'pick' is invoked, then the change applies to the picked color`() {
        with(sut) {
            val state = initialState(Color.Hsv(hue = 120f, saturation = 0.5f, value = 0.25f))
                .pick { current -> current.copy(hue = 240f) }.state

            val picked = state.pick { current -> current.copy(saturation = 1f, value = 1f) }

            val pickedState = picked.state.shouldBeInstanceOf<ColorInputHsvState.Picked>()
            pickedState.picked shouldBe Color.Hsv(hue = 240f, saturation = 1f, value = 1f)
        }
    }

    @Test
    fun `given 'Settled', when 'pick' is invoked, then it's 'Picked'`() {
        with(sut) {
            val state = initialState(Color.Hsv(hue = 120f, saturation = 0.5f, value = 0.25f))

            val picked = state.pick { current -> current.copy(hue = 240f) }

            picked.state.shouldBeInstanceOf<ColorInputHsvState.Picked>()
        }
    }

    @Test
    fun `given 'Picked', when 'pick' is invoked, then its promotion stays`() {
        with(sut) {
            val state = initialState(Color.Hsv(hue = 120f, saturation = 0.5f, value = 0.25f))
                .pick { current -> current.copy(hue = 240f) }.state
            val promotion = state.awaitedPromotion().shouldNotBeNull()

            val picked = state.pick { current -> current.copy(hue = 250f) }

            picked.state.awaitedPromotion() shouldBe promotion
        }
    }

    @Test
    fun `given 'Picked', when 'completePromotion' is invoked with its promotion, then it's 'Settled' with the picked color`() {
        with(sut) {
            val state = initialState(Color.Hsv(hue = 120f, saturation = 0.5f, value = 0.25f))
                .pick { current -> current.copy(hue = 240f) }.state
            val promotion = state.awaitedPromotion().shouldNotBeNull()

            val completed = state.completePromotion(promotion)

            val settled = completed.state.shouldBeInstanceOf<ColorInputHsvState.Settled>()
            settled.color shouldBe Color.Hsv(hue = 240f, saturation = 0.5f, value = 0.25f)
        }
    }

    @Test
    fun `given 'Picked', when 'completePromotion' is invoked with an earlier promotion, then nothing changes`() {
        with(sut) {
            val first = initialState(Color.Hsv(hue = 120f, saturation = 0.5f, value = 0.25f))
                .pick { current -> current.copy(hue = 240f) }.state
            val earlier = first.awaitedPromotion().shouldNotBeNull()
            val state = first
                .withColor(Color.Hsv(hue = 200f, saturation = 0.2f, value = 0.9f))
                .pick { current -> current.copy(hue = 250f) }.state

            val completed = state.completePromotion(earlier)

            completed shouldBe Change(state)
        }
    }

    @Test
    fun `given 'Picked', when 'withColor' is invoked with another color, then it's 'Settled' with that color`() {
        with(sut) {
            val state = initialState(Color.Hsv(hue = 120f, saturation = 0.5f, value = 0.25f))
                .pick { current -> current.copy(hue = 240f) }.state

            val withColor = state.withColor(Color.Hsv(hue = 200f, saturation = 0.2f, value = 0.9f))

            val settled = withColor.shouldBeInstanceOf<ColorInputHsvState.Settled>()
            settled.color shouldBe Color.Hsv(hue = 200f, saturation = 0.2f, value = 0.9f)
        }
    }

    @Test
    fun `given a gray with another hue, when 'withColor' is invoked with the same gray, then nothing changes`() {
        with(sut) {
            val gray = Color.Hex(0x808080)
            val state = run {
                val picked = initialState(gray).pick { current -> current.copy(hue = 120f) }.state
                val promotion = picked.awaitedPromotion().shouldNotBeNull()
                picked.completePromotion(promotion).state
            }

            val withGray = state.withColor(gray)

            withGray shouldBe state
        }
    }
}
