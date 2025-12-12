package io.github.mmolosay.thecolor.presentation.common

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
class ImmediateRelayTest {

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `given there are no collectors, when values are sent and then collector appears, then the collector receives all previously sent values`() {
        val testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        runTest(testDispatcher) {
            val sut = createSut()

            val value1 = Event.Foo
            sut.send(value1)
            val value2 = Event.Bar("hello")
            sut.send(value2)
            val collectedValues = mutableListOf<Event>()
            sut.flowForView.take(2).toList(collectedValues)

            collectedValues shouldBe listOf(value1, value2)
        }
    }

    @Test
    fun `when two different values are sent concurrently, then both are emitted from the flow and none are lost`() {
        val testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        runTest(testDispatcher) {
            val sut = createSut()

            val collectedValues = mutableListOf<Event>()
            val collectionJob = launch {
                sut.flowForView.take(2).toList(collectedValues)
            }
            advanceUntilIdle()
            val value1 = Event.Foo
            launch { sut.send(value1) }
            val value2 = Event.Bar("hello")
            launch { sut.send(value2) }
            advanceUntilIdle()

            collectedValues shouldBe listOf(value1, value2)
            collectionJob.cancel()
        }
    }

    @Test
    fun `given there is a collector, when it is cancelled and a new collector appears, then next emitted value is delivered to the new collector`() {
        val testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        runTest(testDispatcher) {
            val sut = createSut()
            val collectedValues = mutableListOf<Event>()
            val initialCollectionJob = launch {
                sut.flowForView.toList(collectedValues)
            }
            val value1 = Event.Foo
            sut.send(value1)

            initialCollectionJob.cancel()
            val newCollectionJob = launch {
                sut.flowForView.toList(collectedValues)
            }
            val value2 = Event.Bar("hello")
            sut.send(value2)

            collectedValues shouldBe listOf(value1, value2)
            newCollectionJob.cancel()
        }
    }

    fun createSut() =
        ImmediateRelay<Event>()

    sealed interface Event {
        data object Foo : Event
        data class Bar(val payload: String) : Event
    }
}