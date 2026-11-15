package io.github.mmolosay.thecolor.presentation.input

import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorComparator
import io.github.mmolosay.thecolor.domain.color.ColorConverter
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvCore
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvFeature
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class ColorInputHsvFeatureTest {

    val testDispatcher = UnconfinedTestDispatcher()

    val core = run {
        val colorConverter = ColorConverter()
        ColorInputHsvCore(
            colorConverter = colorConverter,
            colorComparator = ColorComparator(colorConverter),
        )
    }

    val store = run {
        val color = Color.Hsv(hue = 120f, saturation = 0.5f, value = 0.25f)
        Store(initialState = core.initialState(color))
    }

    lateinit var sut: ColorInputHsvFeature

    @Test
    fun `when a hue is picked, then 'color' changes only once the promotion delay has passed`() =
        runTest(testDispatcher) {
            // GIVEN
            createSut(scope = backgroundScope)

            // WHEN-THEN #1
            sut.pick { current -> current.copy(hue = 240f) }
            advanceTimeBy(199.milliseconds) // just short of the promotion delay
            store.value.color shouldBe Color.Hsv(hue = 120f, saturation = 0.5f, value = 0.25f)

            // WHEN-THEN #2
            advanceTimeBy(1.milliseconds)
            runCurrent() // the promotion, due at exactly the delay
            store.value.color shouldBe Color.Hsv(hue = 240f, saturation = 0.5f, value = 0.25f)
        }

    @Test
    fun `given a promotion is pending, when a color is set from elsewhere, then the pick is NOT promoted`() =
        runTest(testDispatcher) {
            createSut(scope = backgroundScope)
            sut.pick { current -> current.copy(hue = 240f) }
            val colorSetFromElsewhere = Color.Hsv(hue = 10f, saturation = 0.2f, value = 0.9f)

            store.update { state ->
                val withColor = with(core) { state.withColor(colorSetFromElsewhere) }
                Change(withColor)
            }
            advanceTimeBy(200.milliseconds) // the promotion delay
            runCurrent()

            store.value.color shouldBe colorSetFromElsewhere
        }

    fun createSut(scope: CoroutineScope) =
        ColorInputHsvFeature(
            core = core,
            store = store,
            scope = scope,
        ).also {
            sut = it
        }
}
