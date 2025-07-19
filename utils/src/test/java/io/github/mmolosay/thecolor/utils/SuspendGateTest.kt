package io.github.mmolosay.thecolor.utils

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class SuspendGateTest {

    val testDispatcher = UnconfinedTestDispatcher()

    lateinit var sut: ClosableSuspendGate

    @Test
    fun `given gate is closed and 'awaitOpen()' is called in a coroutine, when the coroutine is cancelled, then gate stays closed`() =
        runTest(testDispatcher) {
            sut = ClosableSuspendGate(closed = true)
            val coroutineJob = launch { sut.awaitOpen() }

            coroutineJob.cancel()

            sut.isClosed shouldBe true
        }
}