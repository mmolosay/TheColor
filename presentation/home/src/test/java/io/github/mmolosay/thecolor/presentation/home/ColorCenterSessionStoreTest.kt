package io.github.mmolosay.thecolor.presentation.home

import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.presentation.home.viewmodel.ColorCenterSession
import io.github.mmolosay.thecolor.presentation.home.viewmodel.ColorCenterSessionStore
import io.github.mmolosay.thecolor.presentation.home.viewmodel.ColorCenterSessionStore.SessionBuildingScope
import io.github.mmolosay.thecolor.presentation.home.viewmodel.ColorCenterSessionStore.SessionState
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beOfType
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class ColorCenterSessionStoreTest {

    val testDispatcher = UnconfinedTestDispatcher()

    lateinit var sut: ColorCenterSessionStore

    @Test
    fun `when 'clear' is called, then the state becomes 'NoSession'`() =
        runTest(testDispatcher) {
            createSut()
            launch {
                sut.startBuilding(seed = Color.Hex(0x0), job = coroutineContext.job)
            }

            sut.clear()

            sut.sessionState shouldBe SessionState.NoSession
        }

    @Test
    fun `when 'start building' is called, then the state becomes 'BeingBuilt'`() =
        runTest(testDispatcher) {
            createSut()

            val seed = Color.Hex(0x0)
            val job = launch {
                sut.startBuilding(seed = seed, job = coroutineContext.job)
            }

            sut.sessionState shouldBe SessionState.BeingBuilt(seed = seed, job = job)
        }

    @Test
    fun `given current state is 'BeingBuilt', when 'clear' is called, then the job of the current state is cancelled`() =
        runTest(testDispatcher) {
            createSut()
            val jobOfBuildingSession = launch {
                sut.startBuilding(seed = Color.Hex(0x0), job = coroutineContext.job)
                suspendCancellableCoroutine {} // suspends indefinitely
            }

            sut.clear()

            jobOfBuildingSession.isCancelled shouldBe true
        }

    @Test
    fun `given current state is 'BeingBuilt', when 'start building' is called, then the job of the current state is cancelled`() =
        runTest(testDispatcher) {
            createSut()
            val jobOfBuildingSession = launch {
                sut.startBuilding(seed = Color.Hex(0x0), job = coroutineContext.job)
                suspendCancellableCoroutine {} // suspends indefinitely
            }

            sut.startBuilding(seed = Color.Hex(0x1), job = coroutineContext.job)

            jobOfBuildingSession.isCancelled shouldBe true
        }

    @Test
    fun `given current state is 'BeingBuilt', when 'complete' is called on the building scope, then the state becomes 'Ongoing'`() =
        runTest(testDispatcher) {
            createSut()
            val seed = Color.Hex(0x0)
            val session = ColorCenterSession(seed = seed, relatedColors = emptySet())

            launch {
                val sessionBuilding = sut.startBuilding(seed = seed, job = coroutineContext.job)
                sessionBuilding.complete(session)
            }

            sut.sessionState shouldBe SessionState.Ongoing(session)
        }

    @Test
    fun `given current state is 'BeingBuilt', when 'complete' is called on the building scope, then the job of the building state is not cancelled`() =
        runTest(testDispatcher) {
            createSut()
            val seed = Color.Hex(0x0)

            val jobOfBuildingSession = launch {
                val sessionBuilding = sut.startBuilding(seed = seed, job = coroutineContext.job)
                val session = ColorCenterSession(seed = seed, relatedColors = emptySet())
                sessionBuilding.complete(session)
                suspendCancellableCoroutine {} // suspends indefinitely
            }

            jobOfBuildingSession.isCancelled shouldBe false
            jobOfBuildingSession.cancel()
        }

    /**
     * Tests that [SessionBuildingScope.complete] is cancellation-cooperative.
     * If the [SessionState.BeingBuilt.job] is already canceled, then the original [CancellationException]
     * is re-thrown.
     */
    @Test
    fun `when 'complete' is called on the building scope which job is already canceled, then it re-throws original 'CancellationException'`() =
        runTest(testDispatcher) {
            createSut()
            var sessionBuilding: SessionBuildingScope? = null
            launch {
                sessionBuilding = sut.startBuilding(seed = Color.Hex(0x0), job = coroutineContext.job)
                suspendCancellableCoroutine {} // suspends indefinitely
            }
            sut.clear() // will cancel the job of the building scope

            shouldThrow<CancellationException> {
                sessionBuilding!!.complete(session = mockk())
            }
        }

    /**
     * Tests that [SessionBuildingScope.complete] is cancellation-cooperative.
     * If the [SessionState.BeingBuilt.job] is already canceled, then the method is no-op.
     */
    @Test
    fun `when 'complete' is called on the building scope which job is already canceled, then it doesn't change current state'`() =
        runTest(testDispatcher) {
            createSut()
            var sessionBuilding: SessionBuildingScope? = null
            launch {
                sessionBuilding = sut.startBuilding(seed = Color.Hex(0x0), job = coroutineContext.job)
                suspendCancellableCoroutine {} // suspends indefinitely
            }
            sut.clear() // will cancel the job of the building scope

            val currentState = sut.sessionState
            currentState should beOfType<SessionState.NoSession>()
            try {
                sessionBuilding!!.complete(session = mockk())
            } catch (_: CancellationException) {}
            sut.sessionState shouldBe currentState // unchanged
        }

    fun createSut(): ColorCenterSessionStore =
        ColorCenterSessionStore().also { sut = it }

    val ColorCenterSessionStore.sessionState: SessionState
        get() = this.flowOfSessionState.value
}