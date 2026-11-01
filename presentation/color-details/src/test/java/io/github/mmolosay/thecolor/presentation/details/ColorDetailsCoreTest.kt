package io.github.mmolosay.thecolor.presentation.details

import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsCore
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsOutcome
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsState
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorRole
import io.github.mmolosay.thecolor.presentation.details.viewmodel.CreateColorDetailsDataUseCase
import io.github.mmolosay.thecolor.presentation.details.viewmodel.CreateSubjectUseCase
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import io.github.mmolosay.thecolor.domain.color.ColorDetails as DomainColorDetails

class ColorDetailsCoreTest {

    // only 'toViewData' uses them, and these tests cover the transitions
    val createData: CreateColorDetailsDataUseCase = mockk()
    val createSubject: CreateSubjectUseCase = mockk()

    val sut = ColorDetailsCore(
        createData = createData,
        createSubject = createSubject,
    )

    @Test
    fun `given the seed's request is awaited, when 'completeDetails' is invoked with a success, then the state is 'Ready' with the role 'Seed'`() {
        with(sut) {
            val color = Color.Hex(0x1A803F)
            val state = initialState().withSession(color)
            val request = state.awaited().shouldNotBeNull()

            val details = ColorDetails(
                color = color,
                exactColor = Color.Hex(0x126B40),
            )
            val completed = state.completeDetails(Result.success(details), request)

            val stateAsReady = completed.state.shouldBeInstanceOf<ColorDetailsState.Ready>()
            stateAsReady.role shouldBe ColorRole.Seed
        }
    }

    @Test
    fun `given the seed's request is awaited, when 'completeDetails' is invoked with a failure, then the state is 'Error' with that request`() {
        with(sut) {
            val state = initialState().withSession(Color.Hex(0x1A803F))
            val request = state.awaited().shouldNotBeNull()

            val completed = state.completeDetails(Result.failure(Exception()), request)

            val stateAsError = completed.state.shouldBeInstanceOf<ColorDetailsState.Error>()
            stateAsError.request shouldBe request
        }
    }

    @Test
    fun `given a newer request is awaited, when 'completeDetails' is invoked for the older one, then nothing changes`() {
        with(sut) {
            val color = Color.Hex(0x1A803F)
            val older = initialState().withSession(color)
            val olderRequest = older.awaited().shouldNotBeNull()
            val newer = older.withSession(color)

            val details = ColorDetails(
                color = color,
                exactColor = Color.Hex(0x126B40),
            )
            val completed = newer.completeDetails(Result.success(details), olderRequest)

            completed shouldBe Change(newer)
        }
    }

    @Test
    fun `given there's no session, when 'select' is invoked, then nothing changes`() {
        with(sut) {
            val state = initialState()

            val selected = state.select(ColorRole.Exact)

            selected shouldBe Change(state)
        }
    }

    @Test
    fun `given the seed's details are in, when 'select' is invoked with 'Exact', then the exact color's request is awaited`() {
        with(sut) {
            val details = ColorDetails(
                color = Color.Hex(0x1A803F),
                exactColor = Color.Hex(0x126B40),
            )
            val state = initialState().withSeedDetails(details)

            val selected = state.select(ColorRole.Exact)

            val awaited = selected.state.awaited().shouldNotBeNull()
            awaited.color shouldBe details.exact.color
            awaited.role shouldBe ColorRole.Exact
        }
    }

    @Test
    fun `given the seed's details are in, when 'select' is invoked with 'Exact', then 'ColorSelected' is handed up with the exact color`() {
        with(sut) {
            val details = ColorDetails(
                color = Color.Hex(0x1A803F),
                exactColor = Color.Hex(0x126B40),
            )
            val state = initialState().withSeedDetails(details)

            val selected = state.select(ColorRole.Exact)

            selected.outcome shouldBe ColorDetailsOutcome.ColorSelected(details.exact.color)
        }
    }

    @Test
    fun `given the exact color's details are in, when 'select' is invoked with 'Exact' after 'Seed', then nothing is awaited`() {
        with(sut) {
            val exactColor = Color.Hex(0x126B40)
            val state = initialState()
                .let { state ->
                    val seedDetails = ColorDetails(color = Color.Hex(0x1A803F), exactColor = exactColor)
                    state.withSeedDetails(seedDetails)
                }
                .select(ColorRole.Exact).state
                .let { state ->
                    val request = state.awaited().shouldNotBeNull()
                    val exactDetails = ColorDetails(color = exactColor, exactColor = exactColor)
                    state.completeDetails(Result.success(exactDetails), request).state
                }
                .select(ColorRole.Seed).state

            val selected = state.select(ColorRole.Exact)

            selected.state.awaited().shouldBeNull()
        }
    }

    @Test
    fun `given a fetch failed, when 'retry' is invoked, then a new request for the same color and role is awaited`() {
        with(sut) {
            // a role other than 'Seed', so that the test can't pass with the seed's request
            val state = initialState()
                .let { state ->
                    val details = ColorDetails(
                        color = Color.Hex(0x1A803F),
                        exactColor = Color.Hex(0x126B40),
                    )
                    state.withSeedDetails(details)
                }
                .select(ColorRole.Exact).state
            val failedRequest = state.awaited().shouldNotBeNull()
            val failed = state.completeDetails(Result.failure(Exception()), failedRequest).state

            val retried = failed.retry()

            val awaited = retried.state.awaited().shouldNotBeNull()
            awaited shouldNotBe failedRequest
            awaited.color shouldBe failedRequest.color
            awaited.role shouldBe failedRequest.role
        }
    }

    fun ColorDetails(color: Color, exactColor: Color): DomainColorDetails =
        mockk {
            every { this@mockk.color } returns color
            every { exact.color } returns exactColor
        }
}
