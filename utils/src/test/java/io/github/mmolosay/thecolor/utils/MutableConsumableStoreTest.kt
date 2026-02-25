package io.github.mmolosay.thecolor.utils

import io.github.mmolosay.thecolor.utils.ConsumableStore.Entry
import io.github.mmolosay.thecolor.utils.ConsumableStore.Id
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.collections.shouldNotContainDuplicates
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeTypeOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
internal class MutableConsumableStoreImplTest {

    val testDispatcher = UnconfinedTestDispatcher()

    lateinit var sut: MutableConsumableStoreImpl<String>

    @Test
    fun `when SUT is created, the list of pending entries is empty`() =
        runTest(testDispatcher) {
            sut = MutableConsumableStoreImpl()

            sut.pending.shouldBeEmpty()
        }

    @Test
    fun `publishing a value emits a new list of pending entries with the published value added`() =
        runTest(testDispatcher) {
            sut = MutableConsumableStoreImpl()
            var emittedPending: List<Entry<String>>? = null
            val flowCollectionJob = launch {
                emittedPending = sut.flowOfPending.firstNext()
            }
            emittedPending shouldBe null

            val value = "Value 1"
            sut.publish(value)

            emittedPending.shouldNotBeNull().values() shouldContain value
            flowCollectionJob.cancel()
        }

    @Test
    fun `publishing multiple values adds all of them to the list of pending values`() =
        runTest(testDispatcher) {
            sut = MutableConsumableStoreImpl()

            sut.publish("Value 1")
            sut.publish("Value 2")
            sut.publish("Value 3")

            sut.pending.values() shouldContainExactly listOf("Value 1", "Value 2", "Value 3")
        }

    @Test
    fun `publishing same value multiple times adds all of them to the list of pending values with unique IDs`() =
        runTest(testDispatcher) {
            sut = MutableConsumableStoreImpl()

            val value = "Value"
            sut.publish(value)
            sut.publish(value)
            sut.publish(value)

            sut.pending.values() shouldContainExactly listOf(value, value, value)
            sut.pending.ids().shouldNotContainDuplicates()
        }

    /**
     * Tests that [MutableConsumableStoreImpl.flowOfPending] is updated with an immutable instance
     * of [List] when a value is published.
     * This way the client can't perform a type cast to the actual mutable instance and modify
     * the list from outside.
     */
    @Test
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    fun `publishing a value updates the flow of pending values with immutable list`() =
        runTest(testDispatcher) {
            sut = MutableConsumableStoreImpl()

            sut.publish("Value 1")
            sut.publish("Value 2")
            sut.publish("Value 3")

            val pending = sut.pending
            pending.shouldNotBeTypeOf<MutableList<*>>()
            pending.shouldNotBeTypeOf<ArrayList<*>>()
            if (pending is java.util.List<*>) {
                shouldThrow<UnsupportedOperationException> {
                    // java's List may be mutable, need to attempt to modify it
                    pending.remove(pending.size - 1)
                }
            }
        }

    @RepeatedTest(10) // executed sequentially (by default)
    fun `concurrent publishing results in sequential values of new pending IDs`() =
        runTest(testDispatcher) {
            sut = MutableConsumableStoreImpl()

            val numberOfThreads = 32
            val barrier = CyclicBarrier(numberOfThreads)
            val executor = Executors.newFixedThreadPool(numberOfThreads)
            repeat(numberOfThreads) { threadIndex ->
                executor.submit {
                    barrier.await()
                    sut.publish("Value $threadIndex")
                }
            }
            executor.shutdown()
            executor.awaitTermination(1, TimeUnit.SECONDS)
            runCurrent()

            val expectedIds = (0 until numberOfThreads).map { Id(it) }
            sut.pending.ids() shouldContainExactly expectedIds
        }

    @RepeatedTest(10) // executed sequentially (by default)
    fun `concurrent publishing results in all published values being added to the list of pending values`() =
        runTest(testDispatcher) {
            val numberOfThreads = 32
            val values = List(size = numberOfThreads) { index -> "Value $index" }
            sut = MutableConsumableStoreImpl()

            val barrier = CyclicBarrier(numberOfThreads)
            val executor = Executors.newFixedThreadPool(numberOfThreads)
            repeat(numberOfThreads) { threadIndex ->
                executor.submit {
                    barrier.await()
                    sut.publish(values[threadIndex])
                }
            }
            executor.shutdown()
            executor.awaitTermination(1, TimeUnit.SECONDS)
            runCurrent()

            sut.pending.values() shouldContainExactlyInAnyOrder values
        }

    @Test
    fun `consuming a pending value by ID removes it from the list of pending values`() =
        runTest(testDispatcher) {
            sut = MutableConsumableStoreImpl()

            val value = "Value 1"
            sut.publish(value)
            sut.pending.values() shouldContain value
            val valueId = sut.pending.last { it.value == value }.id
            sut.consume(valueId)

            sut.pending.values() shouldNotContain value
        }

    @Test
    fun `consuming a present pending value by ID returns 'true'`() =
        runTest(testDispatcher) {
            sut = MutableConsumableStoreImpl()

            val value = "Value 1"
            sut.publish(value)
            sut.pending.values() shouldContain value
            val valueId = sut.pending.last { it.value == value }.id
            val wasRemoved = sut.consume(valueId)

            wasRemoved shouldBe true
        }

    @Test
    fun `consuming an absent pending value by ID returns 'false'`() =
        runTest(testDispatcher) {
            sut = MutableConsumableStoreImpl()

            val value = "Value 1"
            sut.publish(value)
            sut.pending.values() shouldContain value
            val valueId = sut.pending.last { it.value == value }.id
            sut.consume(valueId) // consume 1st time: removed, returns 'true'
            val wasRemoved = sut.consume(valueId) // consume 2nd time: absent, returns 'false'

            wasRemoved shouldBe false
        }

    @Test
    fun `consuming a pending value by ID emits a new list of pending values without the consumed value`() =
        runTest(testDispatcher) {
            // GIVEN
            sut = MutableConsumableStoreImpl()

            // WHEN
            val value = "Value 1"
            sut.publish(value)
            val valueId = sut.pending.last { it.value == value }.id

            var emittedPending: List<Entry<String>>? = null
            val flowCollectionJob = launch {
                emittedPending = sut.flowOfPending.firstNext()
            }
            emittedPending shouldBe null

            sut.consume(valueId)

            // THEN
            emittedPending.shouldNotBeNull().values() shouldNotContain value
            flowCollectionJob.cancel()
        }

    @Test
    fun `consuming an absent pending value by ID doesn't update the list of pending values`() =
        runTest(testDispatcher) {
            sut = MutableConsumableStoreImpl()

            val value = "Value 1"
            sut.publish(value)
            sut.pending.values() shouldContain value
            val valueId = sut.pending.last { it.value == value }.id
            sut.consume(valueId) // consume 1st time: removed, 'pending' is updated
            val pending1 = sut.pending
            sut.consume(valueId) // consume 1st time: absent, 'pending' is unchanged

            val pending2 = sut.pending
            pending2 shouldBeSameInstanceAs pending1
        }

    /**
     * Tests that [.asConsumableStore] returns a truly immutable instance
     * of [ConsumableStore].
     * This way the client can't perform a type cast to the actual mutable instance and modify
     * the store from outside.
     */
    @Test
    fun `converting mutable instance to immutable one produces truly immutable instance`() =
        runTest(testDispatcher) {
            sut = MutableConsumableStoreImpl()

            val asImmutable = sut.asConsumableStore()

            asImmutable.shouldNotBeTypeOf<MutableConsumableStore<*>>()
        }
}