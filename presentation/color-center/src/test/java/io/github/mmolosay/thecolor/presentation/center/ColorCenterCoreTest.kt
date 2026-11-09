package io.github.mmolosay.thecolor.presentation.center

import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.presentation.center.viewmodel.ColorCenterCore
import io.github.mmolosay.thecolor.presentation.center.viewmodel.ColorCenterOutcome
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsCore
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsOutcome
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsState
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorRole
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeCore
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeOutcome
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeState
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import io.github.mmolosay.thecolor.domain.color.ColorDetails as DomainColorDetails
import io.github.mmolosay.thecolor.domain.color.ColorScheme as DomainColorScheme

class ColorCenterCoreTest {

    val detailsFactory = ColorDetailsCore.Factory {
        ColorDetailsCore(
            createData = mockk(),
            createSubject = mockk(),
        )
    }

    val schemeFactory = ColorSchemeCore.Factory {
        ColorSchemeCore(
            createSwatch = mockk(),
        )
    }

    val sut = ColorCenterCore(
        detailsFactory = detailsFactory,
        schemeFactory = schemeFactory,
    )

    @Test
    fun `when 'withSession' is invoked, then 'details' awaits the seed`() {
        with(sut) {
            val color = Color.Hex(0x1A803F)

            val state = initialState().withSession(color)

            val awaited = with(detailsCore) { state.details.awaited() }.shouldNotBeNull()
            awaited.color shouldBe color
        }
    }

    @Test
    fun `when 'withSession' is invoked, then 'scheme' awaits the seed`() {
        with(sut) {
            val color = Color.Hex(0x1A803F)

            val state = initialState().withSession(color)

            val awaited = with(schemeCore) { state.scheme.awaited() }.shouldNotBeNull()
            awaited.seed shouldBe color
        }
    }

    @Test
    fun `given a session is open, when 'withoutSession' is invoked, then 'details' and 'scheme' are 'NotRequested'`() {
        with(sut) {
            val state = initialState().withSession(Color.Hex(0x1A803F))

            val ended = state.withoutSession()

            ended.details shouldBe ColorDetailsState.NotRequested
            ended.scheme shouldBe ColorSchemeState.NotRequested
        }
    }

    @Test
    fun `given the exact color is selected in 'details', when 'sessionColor' is invoked, then it's the exact color`() {
        with(sut) {
            val seedColor = Color.Hex(0x1A803F)
            val exactColor = Color.Hex(0x126B40)
            val seedDetails = mockk<DomainColorDetails> {
                every { color } returns seedColor
                every { exact.color } returns exactColor
            }
            val state = initialState()
                .withSession(seedColor)
                .let { state ->
                    val details = with(detailsCore) {
                        val request = state.details.awaited().shouldNotBeNull()
                        state.details
                            .completeDetails(Result.success(seedDetails), request).state
                            .select(ColorRole.Exact).state
                    }
                    state.copy(details = details)
                }

            val sessionColor = state.sessionColor()

            sessionColor shouldBe exactColor
        }
    }

    @Test
    fun `given a mode is selected in 'scheme', when 'onDetails' is invoked with 'ColorSelected', then 'scheme' awaits that color in the selected mode`() {
        with(sut) {
            // a mode other than the default, so that a new session's request can't pass the test
            val state = initialState()
                .withSession(Color.Hex(0x1A803F))
                .let { state ->
                    val scheme = with(schemeCore) {
                        val request = state.scheme.awaited().shouldNotBeNull()
                        val result = Result.success(mockk<DomainColorScheme>())
                        state.scheme
                            .completeScheme(result, request).state
                            .selectMode(DomainColorScheme.Mode.Analogic).state
                    }
                    state.copy(scheme = scheme)
                }

            val color = Color.Hex(0x126B40)
            val handled = state.onDetails(ColorDetailsOutcome.ColorSelected(color))

            val awaited = with(schemeCore) { handled.state.scheme.awaited() }.shouldNotBeNull()
            awaited.seed shouldBe color
            awaited.mode shouldBe DomainColorScheme.Mode.Analogic
        }
    }

    @Test
    fun `given a session is open, when 'onDetails' is invoked with 'ColorSelected', then 'SessionColorSelected' is handed up with that color`() {
        with(sut) {
            val state = initialState().withSession(Color.Hex(0x1A803F))

            val color = Color.Hex(0x126B40)
            val handled = state.onDetails(ColorDetailsOutcome.ColorSelected(color))

            handled.outcome shouldBe ColorCenterOutcome.SessionColorSelected(color)
        }
    }

    @Test
    fun `given a session is open, when 'onScheme' is invoked with 'SwatchSelected', then 'SwatchSelected' is handed up with the swatch's details`() {
        with(sut) {
            val state = initialState().withSession(Color.Hex(0x1A803F))

            val details = mockk<DomainColorDetails>()
            val handled = state.onScheme(ColorSchemeOutcome.SwatchSelected(details))

            handled.outcome shouldBe ColorCenterOutcome.SwatchSelected(details)
        }
    }
}
