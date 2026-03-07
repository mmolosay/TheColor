package io.github.mmolosay.thecolor.presentation.home

import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.presentation.home.viewmodel.ColorCenterSession
import io.github.mmolosay.thecolor.presentation.home.viewmodel.ColorCenterSessionStore
import io.github.mmolosay.thecolor.presentation.home.viewmodel.ColorCenterSessionStore.SessionState
import io.github.mmolosay.thecolor.utils.ClosableSuspendGate
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
                sut.startBuilding(seed = Color.Hex(0x0))
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
                sut.startBuilding(seed)
            }

            sut.sessionState shouldBe SessionState.BeingBuilt(seed = seed, job = job)
        }

    @Test
    fun `given current state is 'BeingBuilt', when 'clear' is called, then the job of the current state is cancelled`() =
        runTest(testDispatcher) {
            createSut()
            val jobOfBuildingSession = launch {
                sut.startBuilding(seed = Color.Hex(0x0))
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
                sut.startBuilding(seed = Color.Hex(0x0))
                suspendCancellableCoroutine {} // suspends indefinitely
            }

            sut.startBuilding(seed = Color.Hex(0x1))

            jobOfBuildingSession.isCancelled shouldBe true
        }

    @Test
    fun `given current state is 'BeingBuilt', when 'complete' is called on the building scope, then the state becomes 'Ongoing'`() =
        runTest(testDispatcher) {
            createSut()
            val seed = Color.Hex(0x0)
            val session = ColorCenterSession(seed = seed, relatedColors = emptySet())

            launch {
                sut.startBuilding(seed).run {
                    complete(session)
                }
            }

            sut.sessionState shouldBe SessionState.Ongoing(session)
        }

    @Test
    fun `given current state is 'BeingBuilt', when 'complete' is called on the building scope, then the job of the building state is not cancelled`() =
        runTest(testDispatcher) {
            createSut()
            val seed = Color.Hex(0x0)

            val jobOfBuildingSession = launch {
                sut.startBuilding(seed).run {
                    val session = ColorCenterSession(seed = seed, relatedColors = emptySet())
                    complete(session)
                    suspendCancellableCoroutine {} // suspends indefinitely
                }
            }

            jobOfBuildingSession.isCancelled shouldBe false
            jobOfBuildingSession.cancel()
        }

    /**
     * Tests that "stale" [ColorCenterSessionStore.SessionBuildingScope] cannot be used to
     * complete building a session.
     *
     * Example:
     * 1. [ColorCenterSessionStore.startBuilding] is called.
     * It returns [ColorCenterSessionStore.SessionBuildingScope] which we will call S.
     * 2. [ColorCenterSessionStore.clear] is called.
     * 3. [ColorCenterSessionStore.SessionBuildingScope.complete] is called on the S.
     *
     * Due to step 2 interrupting the 'BeingBuilt -> Ongoing' session state pipeline, scope S
     * is considered "stale".
     */
    @Test
    fun `given current state is 'NoSession', when 'complete' is called on the building scope, then an exception is thrown`() =
        runTest(testDispatcher) {
            createSut()
            val gate = ClosableSuspendGate(closed = true)

            launch {
                val sessionBuildingScope = sut.startBuilding(seed = Color.Hex(0x0))
                gate.awaitOpen()
                shouldThrowAny {
                    sessionBuildingScope.complete(session = mockk<ColorCenterSession>())
                }
            }
            launch {
                sut.clear()
                gate.open()
            }
        }

    /**
     * Test the same thing as the test above.
     */
    @Test
    fun `given current state is 'BeingBuilt', when 'complete' is called on the old building scope, then an exception is thrown`() =
        runTest(testDispatcher) {
            createSut()

            var scope1: ColorCenterSessionStore.SessionBuildingScope? = null
            launch {
                scope1 = sut.startBuilding(seed = Color.Hex(0x0))
            }

            launch {
                @Suppress("UnusedVariable")
                val scope2 = sut.startBuilding(seed = Color.Hex(0x0)) // seed doesn't matter in this test
                shouldThrowAny {
                    scope1!!.complete(session = mockk<ColorCenterSession>())
                }
            }
        }

    fun createSut(): ColorCenterSessionStore =
        ColorCenterSessionStore().also { sut = it }

    val ColorCenterSessionStore.sessionState: SessionState
        get() = this.flowOfSessionState.value
}