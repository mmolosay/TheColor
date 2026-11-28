package io.github.mmolosay.thecolor.utils

import io.github.mmolosay.thecolor.utils.CoroutineRegistry.Item
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Job
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.util.concurrent.Callable
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.max


@Timeout(value = 30, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class CoroutineRegistryTest {

    lateinit var sut: CoroutineRegistry<String>

    /**
     * Tests that [CoroutineRegistry.access] can only be entered from a single thread at a time,
     * thus critical section and updates are synchronized and safe for concurrency.
     */
    @RepeatedTest(10) // executed sequentially (by default)
    fun `when SUT is accessed from multiple threads concurrently, then the critical section is synchronized`() {
        sut = CoroutineRegistry<String>()
        val numberOfThreads = 16
        val executor = Executors.newFixedThreadPool(numberOfThreads)
        val barrier = CyclicBarrier(numberOfThreads)
        // atomics to have accurate values in case if test fails and the critical section is not synchronized
        val activeConcurrentExecutions = AtomicInteger(0)
        val maxConcurrentExecutions = AtomicInteger(0)
        val tasks = List(numberOfThreads) {
            Callable {
                barrier.await()
                sut.access {
                    val active = activeConcurrentExecutions.incrementAndGet()
                    maxConcurrentExecutions.updateAndGet { max(it, active) }
                    // widen the time window of the critical section to give other threads a bigger chance to enter it if it's not synchronized
                    Thread.sleep(10)
                    activeConcurrentExecutions.decrementAndGet()
                }
            }
        }

        try {
            val results = executor.invokeAll(tasks)
            results.forEach { it.get() } // rethrows what has been thrown in the threads
        } finally {
            executor.shutdown()
        }

        activeConcurrentExecutions.get() shouldBe 0
        maxConcurrentExecutions.get() shouldBe 1
    }

    @Test
    fun `given an 'access' block is in progress, when a nested 'access' block is entered on the same thread, then it does not deadlock and observes mutations of the outer block`() {
        sut = CoroutineRegistry<String>()

        // if the nested access deadlocks, then the test fails on the class' timeout, not on an assertion
        val itemsInNestedBlock = sut.access {
            add(Job(), "1")
            sut.access { items }
        }

        itemsInNestedBlock.map { it.value } shouldContainExactly listOf("1")
    }

    @Test
    fun `given a nested 'access' block is in progress, when the outer 'AccessProvider' is used, then an exception is thrown`() {
        sut = CoroutineRegistry<String>()

        shouldThrow<IllegalStateException> {
            sut.access {
                val outerAccessProvider = this
                sut.access {
                    // the AccessProvider of this nested block is the only valid one at this point
                    outerAccessProvider.add(Job(), "1")
                }
            }
        }
    }

    @Test
    fun `given a nested 'access' block has returned, when the outer 'AccessProvider' is used, then it is valid again`() {
        sut = CoroutineRegistry<String>()

        shouldNotThrowAny {
            sut.access {
                val outerAccessProvider = this
                sut.access { items }
                outerAccessProvider.add(Job(), "1")
            }
        }
    }

    @Test
    fun `given an 'access' block has finished, when its 'AccessProvider' is used outside of it, then an exception is thrown`() {
        sut = CoroutineRegistry<String>()

        val capturedAccessProvider = sut.access { this }

        shouldThrow<IllegalStateException> {
            capturedAccessProvider.add(Job(), "1")
        }
    }

    @Test
    fun `given an 'AccessProvider' of a finished 'access' block, when it is used inside another 'access' block, then an exception is thrown`() {
        sut = CoroutineRegistry<String>()

        val capturedAccessProvider = sut.access { this }

        shouldThrow<IllegalStateException> {
            sut.access {
                // using foreign access provider, not the one from this 'access' block
                capturedAccessProvider.add(Job(), "1")
            }
        }
    }

    @Test
    fun `given an exception is thrown inside an 'access' block, when it is caught outside, then the lock is released`() {
        sut = CoroutineRegistry<String>()

        try {
            sut.access {
                add(Job(), "1")
                error("exception")
            }
        } catch (_: IllegalStateException) {}

        sut.isAccessibleFromAnotherThread() shouldBe true
    }

    @Test
    fun `given an exception is thrown inside a nested 'access' block, when it is caught in the outer one, then the outer 'AccessProvider' is valid again`() {
        sut = CoroutineRegistry<String>()

        shouldNotThrowAny {
            sut.access {
                val outerAccessProvider = this
                try {
                    sut.access { error("exception") }
                } catch (_: IllegalStateException) {}
                outerAccessProvider.add(Job(), "1")
            }
        }
    }

    @Test
    fun `given 'items' were read inside an 'access' block, when the registry is mutated afterwards, then the previously read list is unchanged`() {
        sut = CoroutineRegistry<String>()

        val itemsReadBeforeMutation = sut.access {
            add(Job(), "1")
            val snapshot = items
            add(Job(), "2")
            snapshot
        }

        itemsReadBeforeMutation.map { it.value } shouldContainExactly listOf("1")
    }

    @Test
    fun `given a registered job, when an unregistered job is removed, then 'null' is returned and the 'items' are unchanged`() {
        sut = CoroutineRegistry<String>()
        val registeredItem = sut.access { add(Job(), "1") }

        val removedItem = sut.access { remove(Job()) }

        removedItem.shouldBeNull()
        sut.access { items } shouldContainExactly listOf(registeredItem)
    }

    @Test
    fun `given several items with the same value, when one of them is removed, then the others remain registered`() {
        sut = CoroutineRegistry<String>()
        val value = "1"
        val firstItem = sut.access { add(Job(), value) }
        val secondItem = sut.access { add(Job(), value) }
        val thirdItem = sut.access { add(Job(), value) }

        val removedItem = sut.access { remove(secondItem.job) }

        removedItem shouldBe secondItem
        sut.access { items } shouldContainExactly listOf(firstItem, thirdItem)
    }
}

/**
 * Tries to enter a [CoroutineRegistry.access] block from a different thread and reports whether it has
 * succeeded within the [timeoutMillis].
 *
 * The lock is reentrant, so a thread that holds it cannot tell that it does.
 * Only a different thread can.
 */
fun CoroutineRegistry<*>.isAccessibleFromAnotherThread(timeoutMillis: Long = 1_000): Boolean {
    val thread = Thread { this.access { } }.apply { isDaemon = true }
    thread.start()
    thread.join(timeoutMillis)
    return !thread.isAlive
}

fun <T> CoroutineRegistry<T>.items(): List<Item<T>> =
    access { items }
