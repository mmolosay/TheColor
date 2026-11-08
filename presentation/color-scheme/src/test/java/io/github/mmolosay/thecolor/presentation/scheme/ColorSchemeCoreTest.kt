package io.github.mmolosay.thecolor.presentation.scheme

import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeCore
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeOutcome
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeState
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeViewData
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.CreateSwatchUseCase
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.SwatchCount
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import org.junit.jupiter.api.Test
import io.github.mmolosay.thecolor.domain.color.ColorDetails as DomainColorDetails
import io.github.mmolosay.thecolor.domain.color.ColorScheme as DomainColorScheme

class ColorSchemeCoreTest {

    // only 'toViewData' uses it, to convert the swatches, and these tests don't check them
    val createSwatch: CreateSwatchUseCase = mockk()

    val sut = ColorSchemeCore(
        createSwatch = createSwatch,
    )

    @Test
    fun `when 'withSession' is invoked, then its request has the default mode and swatch count`() {
        with(sut) {
            val state = initialState().withSession(Color.Hex(0x1A803F))

            val awaited = state.awaited().shouldNotBeNull()
            awaited.mode shouldBe DomainColorScheme.Mode.Monochrome
            awaited.swatchCount shouldBe SwatchCount.Six
        }
    }

    @Test
    fun `given a mode is selected, when 'withSeed' is invoked, then the request has the selected mode`() {
        with(sut) {
            val state = initialState()
                .withSession(Color.Hex(0x1A803F))
                .let { state ->
                    val request = state.awaited().shouldNotBeNull()
                    state.completeScheme(Result.success(mockk<DomainColorScheme>()), request).state
                }
                .selectMode(DomainColorScheme.Mode.Analogic).state

            val withNewSeed = state.withSeed(Color.Hex(0x126B40))

            val awaited = withNewSeed.awaited().shouldNotBeNull()
            awaited.mode shouldBe DomainColorScheme.Mode.Analogic
        }
    }

    @Test
    fun `given a mode other than the active one is selected, when 'toViewData' is invoked, then there are changes to apply`() {
        with(sut) {
            val state = initialState()
                .withSession(Color.Hex(0x1A803F))
                .let { state ->
                    val request = state.awaited().shouldNotBeNull()
                    val scheme = DomainColorScheme(swatchDetails = emptyList())
                    state.completeScheme(Result.success(scheme), request).state
                }
                .selectMode(DomainColorScheme.Mode.Analogic).state

            val viewData = state.toViewData()

            val viewDataAsReady = viewData.shouldBeInstanceOf<ColorSchemeViewData.Ready>()
            viewDataAsReady.data.hasChangesToApply shouldBe true
        }
    }

    @Test
    fun `given changes are selected, when 'applyChanges' is invoked, then a request with them is awaited`() {
        with(sut) {
            val state = initialState()
                .withSession(Color.Hex(0x1A803F))
                .let { state ->
                    val request = state.awaited().shouldNotBeNull()
                    state.completeScheme(Result.success(mockk<DomainColorScheme>()), request).state
                }
                .selectMode(DomainColorScheme.Mode.Analogic).state
                .selectSwatchCount(SwatchCount.Nine).state

            val applied = state.applyChanges()

            val awaited = applied.state.awaited().shouldNotBeNull()
            awaited.mode shouldBe DomainColorScheme.Mode.Analogic
            awaited.swatchCount shouldBe SwatchCount.Nine
        }
    }

    @Test
    fun `given nothing new is selected, when 'applyChanges' is invoked, then nothing changes`() {
        with(sut) {
            val state = initialState()
                .withSession(Color.Hex(0x1A803F))
                .let { state ->
                    val request = state.awaited().shouldNotBeNull()
                    state.completeScheme(Result.success(mockk<DomainColorScheme>()), request).state
                }

            val applied = state.applyChanges()

            applied shouldBe Change(state)
        }
    }

    @Test
    fun `given a request is awaited, when 'completeScheme' is invoked with a success, then the state is 'Ready' with the request's mode and swatch count selected`() {
        with(sut) {
            // a mode and a count other than the defaults, so that the test can't pass with them
            val state = initialState()
                .withSession(Color.Hex(0x1A803F))
                .let { state ->
                    val request = state.awaited().shouldNotBeNull()
                    state.completeScheme(Result.success(mockk<DomainColorScheme>()), request).state
                }
                .selectMode(DomainColorScheme.Mode.Analogic).state
                .selectSwatchCount(SwatchCount.Nine).state
                .applyChanges().state
            val request = state.awaited().shouldNotBeNull()

            val scheme = mockk<DomainColorScheme>()
            val completed = state.completeScheme(Result.success(scheme), request)

            val stateAsReady = completed.state.shouldBeInstanceOf<ColorSchemeState.Ready>()
            stateAsReady.selectedMode shouldBe request.mode
            stateAsReady.selectedSwatchCount shouldBe request.swatchCount
        }
    }

    @Test
    fun `given a request is awaited, when 'completeScheme' is invoked with a failure, then the state is 'Error' with that request`() {
        with(sut) {
            val state = initialState().withSession(Color.Hex(0x1A803F))
            val request = state.awaited().shouldNotBeNull()

            val completed = state.completeScheme(Result.failure(Exception()), request)

            val stateAsError = completed.state.shouldBeInstanceOf<ColorSchemeState.Error>()
            stateAsError.request shouldBe request
        }
    }

    @Test
    fun `given a newer request is awaited, when 'completeScheme' is invoked for the older one, then nothing changes`() {
        with(sut) {
            val color = Color.Hex(0x1A803F)
            val older = initialState().withSession(color)
            val olderRequest = older.awaited().shouldNotBeNull()
            val newer = older.withSession(color)

            val scheme = mockk<DomainColorScheme>()
            val completed = newer.completeScheme(Result.success(scheme), olderRequest)

            completed shouldBe Change(newer)
        }
    }

    @Test
    fun `given a scheme is in, when 'selectSwatch' is invoked, then 'SwatchSelected' is handed up with the swatch's details`() {
        with(sut) {
            val swatchDetails = listOf<DomainColorDetails>(mockk(), mockk(), mockk())
            val state = initialState()
                .withSession(Color.Hex(0x1A803F))
                .let { state ->
                    val request = state.awaited().shouldNotBeNull()
                    val scheme = DomainColorScheme(swatchDetails = swatchDetails)
                    state.completeScheme(Result.success(scheme), request).state
                }

            val selected = state.selectSwatch(index = 1)

            selected.outcome shouldBe ColorSchemeOutcome.SwatchSelected(swatchDetails[1])
        }
    }

    @Test
    fun `given a fetch failed, when 'retry' is invoked, then a new request with the same seed, mode and swatch count is awaited`() {
        with(sut) {
            // a mode and a count other than the defaults, so that the test can't pass with them
            val state = initialState()
                .withSession(Color.Hex(0x1A803F))
                .let { state ->
                    val request = state.awaited().shouldNotBeNull()
                    state.completeScheme(Result.success(mockk<DomainColorScheme>()), request).state
                }
                .selectMode(DomainColorScheme.Mode.Analogic).state
                .selectSwatchCount(SwatchCount.Nine).state
                .applyChanges().state
            val failedRequest = state.awaited().shouldNotBeNull()
            val failed = state.completeScheme(Result.failure(Exception()), failedRequest).state

            val retried = failed.retry()

            val awaited = retried.state.awaited().shouldNotBeNull()
            awaited shouldNotBe failedRequest
            awaited.seed shouldBe failedRequest.seed
            awaited.mode shouldBe failedRequest.mode
            awaited.swatchCount shouldBe failedRequest.swatchCount
        }
    }
}
