package io.github.mmolosay.thecolor.utils

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldNotBeTypeOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.max
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class CoroutineRegistryTest {

    val testDispatcher = UnconfinedTestDispatcher()

    lateinit var sut: CoroutineRegistry<String>

    @Test
    fun `when value is added, then it is present in the items`() =
        runTest(testDispatcher) {
            sut = CoroutineRegistry<String>()

            val value = "1"
            sut.add(Job(), value)

            sut.items().any { it.value == value } shouldBe true
        }

    @Test
    fun `when the same value is added multiple times, then all of them are present in the items`() =
        runTest(testDispatcher) {
            sut = CoroutineRegistry<String>()

            val value = "1"
            val job = Job()
            val numberOfDuplicates = 5
            repeat(numberOfDuplicates) {
                sut.add(job, value)
            }

            sut.items().filter { it.value == value }.size shouldBe numberOfDuplicates
        }

    @Test
    fun `when value is removed, then it is no longer present in the items`() =
        runTest(testDispatcher) {
            sut = CoroutineRegistry<String>()

            val value = "1"
            val job = Job()
            val removedItem = sut.access {
                add(job, value)
                remove(job)
            }

            sut.items() shouldNotContain removedItem
        }

    @Test
    fun `when items is removed, then it is returned`() =
        runTest(testDispatcher) {
            sut = CoroutineRegistry<String>()

            val value = "1"
            val job = Job()
            sut.add(job, value)
            val removedItem = sut.remove(job)

            val expectedItem = CoroutineRegistry.Item(
                value = value,
                job = job,
            )
            removedItem.shouldNotBeNull()
            removedItem shouldBe expectedItem
        }

    @Test
    fun `when value is removed, then subsequent attempts to remove it are idempotent`() =
        runTest(testDispatcher) {
            sut = CoroutineRegistry<String>()

            val value = "1"
            val job = Job()
            sut.add(job, value)
            sut.remove(job)
            val items = sut.items()
            shouldNotThrowAny {
                repeat(5) {
                    sut.remove(job)
                }
            }

            sut.items() shouldContainExactly items
        }

    @Test
    fun `when items are accessed, then the list is truly immutable`() =
        runTest(testDispatcher) {
            sut = CoroutineRegistry<String>()

            sut.add(Job(), "1")

            val items = sut.items()
            items.shouldNotBeTypeOf<MutableList<*>>()
            items.shouldNotBeTypeOf<ArrayList<*>>()
        }

    /**
     * Tests that [CoroutineRegistry.access] can only be entered from a single thread (coroutine) at a time,
     * thus critical section and updates are synchronized and safe for concurrency.
     */
    @RepeatedTest(10) // executed sequentially (by default)
    fun `when SUT is accessed from multiple threads concurrently, then the critical section is synchronized`() =
        runTest(testDispatcher) {
            sut = CoroutineRegistry<String>()

            val numberOfThreads = 16
            val dispatcher = Executors.newFixedThreadPool(numberOfThreads).asCoroutineDispatcher()
            val barrier = CyclicBarrier(numberOfThreads)
            // atomics to have accurate values in case if test fails and the critical section is not synchronized
            val activeConcurrentExecutions = AtomicInteger(0)
            val maxConcurrentExecutions = AtomicInteger(0)
            suspend fun executeCoroutine() {
                barrier.await()
                sut.access {
                    val active = activeConcurrentExecutions.incrementAndGet()
                    maxConcurrentExecutions.updateAndGet { max(it, active) }
                    // widen the time window of the critical section to give other threads a bigger chance to enter it if it's not synchronized
                    delay(10.milliseconds)
                    activeConcurrentExecutions.decrementAndGet()
                }
            }
            coroutineScope {
                repeat(numberOfThreads) {
                    launch(dispatcher) {
                        executeCoroutine()
                    }
                }
            }

            activeConcurrentExecutions.get() shouldBe 0
            maxConcurrentExecutions.get() shouldBe 1
            dispatcher.close()
        }

    @Test
    fun `when updates are made outside of the 'access' block, then an exception is thrown`() =
        runTest(testDispatcher) {
            sut = CoroutineRegistry<String>()

            val capturedAccessProvider: CoroutineRegistry<String>.AccessProvider
            sut.access {
                capturedAccessProvider = this
            }

            shouldThrow<IllegalStateException> {
                capturedAccessProvider.add(Job(), "1")
            }
        }

    @Test
    fun `when updates are made via foreign access provider inside the 'access' block, then an exception is thrown`() =
        runTest(testDispatcher) {
            sut = CoroutineRegistry<String>()

            val capturedAccessProvider: CoroutineRegistry<String>.AccessProvider
            sut.access {
                capturedAccessProvider = this
            }

            shouldThrow<IllegalStateException> {
                sut.access {
                    // using foreign access provider, not the one from this 'access' block
                    capturedAccessProvider.add(Job(), "1")
                }
            }
        }

    @Test
    fun `when 'track' is called, then the value is added to the items immediately when the block starts`() =
        runTest(testDispatcher) {
            sut = CoroutineRegistry<String>()

            val value = "1"
            sut.track(Job(), value) {
                sut.items().any { it.value == value } shouldBe true // <- "THEN"
                // some work
            }
        }

    @Test
    fun `when 'track' is called, then the value is removed from the items when the block finishes`() =
        runTest(testDispatcher) {
            sut = CoroutineRegistry<String>()

            val value = "1"
            sut.track(Job(), value) {
                // some work
            }

            sut.items().none { it.value == value } shouldBe true
        }

    @Test
    fun `when 'track' is called and the value is removed before the block finishes, then no exception is thrown`() =
        runTest(testDispatcher) {
            sut = CoroutineRegistry<String>()

            val value = "1"
            val gate = ClosableSuspendGate(closed = true)
            launch {
                sut.trackThis(value) {
                    // some work
                    gate.awaitOpen()
                }
                // exception will be thrown here if the test fails
            }
            launch {
                val job = sut.items().first { it.value == value }.job
                sut.remove(job)
                gate.open() // open the gate to allow 'withRegistry()' to finish executing the block and remove added value
            }
        }

    @Test
    fun `when 'track' is called and an exception is thrown inside the block, then it is re-thrown up the call stack`() =
        runTest(testDispatcher) {
            sut = CoroutineRegistry<String>()

            shouldThrow<IllegalStateException> {
                sut.track(Job(), "1") {
                    // some work
                    error("exception")
                }
            }
        }

    @Test
    fun `when 'track' is called and an exception is thrown inside the block, then the added value is removed nonetheless`() =
        runTest(testDispatcher) {
            sut = CoroutineRegistry<String>()

            val value = "1"
            try {
                sut.track(Job(), value) {
                    // some work
                    error("exception")
                }
            } catch (_: IllegalStateException) {}

            sut.items().none { it.value == value } shouldBe true
        }
}