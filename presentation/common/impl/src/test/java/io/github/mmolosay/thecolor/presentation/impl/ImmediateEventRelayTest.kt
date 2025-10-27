package io.github.mmolosay.thecolor.presentation.impl

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ImmediateEventRelayTest {

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `given there are no collectors, when events are sent and then collector appears, then all sent events are emitted from the flow`() {
        val testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        runTest(testDispatcher) {
            val sut = createSut()

            val event1 = Event.Foo
            sut.send(event1)
            val event2 = Event.Bar("hello")
            sut.send(event2)
            val collectedEvents = mutableListOf<Event>()
            sut.eventFlow.take(2).toList(collectedEvents)

            collectedEvents shouldBe listOf(event1, event2)
        }
    }

    @Test
    fun `when two different events are sent concurrently, then both are emitted from the flow and none are lost`() {
        val testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        runTest(testDispatcher) {
            val sut = createSut()

            val collectedEvents = mutableListOf<Event>()
            val collectionJob = launch {
                sut.eventFlow.take(2).toList(collectedEvents)
            }
            advanceUntilIdle()
            val event1 = Event.Foo
            launch { sut.send(event1) }
            val event2 = Event.Bar("hello")
            launch { sut.send(event2) }
            advanceUntilIdle()

            collectedEvents shouldBe listOf(event1, event2)
            collectionJob.cancel()
        }
    }

    fun createSut() =
        ImmediateEventRelay<Event>()

    sealed interface Event {
        data object Foo : Event
        data class Bar(val payload: String) : Event
    }
}