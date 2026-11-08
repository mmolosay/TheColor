package io.github.mmolosay.thecolor.utils

import io.github.mmolosay.thecolor.utils.CoroutineRegistry.Item
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.max
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class CoroutineRegistryExtTest {

    val testDispatcher = UnconfinedTestDispatcher()

    lateinit var sut: CoroutineRegistry<String>

    // region 'removeOnCompletion'

    @Test
    fun `given a registered job, when it completes normally, then its item is removed`() =
        runTest(testDispatcher) {
            sut = CoroutineRegistry<String>()
            val job = Job()
            sut.access { add(job, "1") }
            sut.removeOnCompletion(job)

            job.complete()

            sut.items().shouldBeEmpty()
        }

    @Test
    fun `given a registered job, when it is cancelled, then its item is removed`() =
        runTest(testDispatcher) {
            sut = CoroutineRegistry<String>()
            val job = Job()
            sut.access { add(job, "1") }
            sut.removeOnCompletion(job)

            job.cancel()

            sut.items().shouldBeEmpty()
        }

    @Test
    fun `given an already completed job, when 'removeOnCompletion' is called, then its item is removed immediately`() =
        runTest(testDispatcher) {
            sut = CoroutineRegistry<String>()
            val job = Job().apply { complete() }
            sut.access { add(job, "1") }

            sut.removeOnCompletion(job)

            sut.items().shouldBeEmpty()
        }

    // endregion

    // region 'supersede'

    @Test
    fun `given items matching the 'predicate', when 'supersede' is called, then they are returned`() =
        runTest(testDispatcher) {
            sut = CoroutineRegistry<String>()
            val matchingItem = sut.access {
                val job = backgroundScope.launch { awaitCancellation() }
                add(job, "old")
            }
            val newJob = backgroundScope.launch { awaitCancellation() }

            val supersededItems = sut.supersede(
                job = newJob,
                value = "new",
                predicate = { it.value == "old" },
            )

            supersededItems shouldContainExactly listOf(matchingItem)
        }

    @Test
    fun `given items matching the 'predicate', when 'supersede' is called, then their jobs are cancelled`() =
        runTest(testDispatcher) {
            sut = CoroutineRegistry<String>()
            val supersededJob = backgroundScope.launch { awaitCancellation() }
            sut.access { add(supersededJob, "old") }

            sut.supersede(
                job = backgroundScope.launch { awaitCancellation() },
                value = "new",
                predicate = { it.value == "old" },
            )

            supersededJob.isCancelled shouldBe true
        }

    @Test
    fun `given items not matching the 'predicate', when 'supersede' is called, then they stay registered and their jobs keep running`() =
        runTest(testDispatcher) {
            sut = CoroutineRegistry<String>()
            val nonMatchingJob = backgroundScope.launch { awaitCancellation() }
            val nonMatchingItem = sut.access { add(nonMatchingJob, "other") }

            sut.supersede(
                job = backgroundScope.launch { awaitCancellation() },
                value = "new",
                predicate = { it.value == "old" },
            )

            sut.items() shouldContain nonMatchingItem
            nonMatchingJob.isActive shouldBe true
        }

    @Test
    fun `when 'supersede' is called, then the new job is registered with its value`() =
        runTest(testDispatcher) {
            sut = CoroutineRegistry<String>()
            val newJob = backgroundScope.launch { awaitCancellation() }

            sut.supersede(
                job = newJob,
                value = "new",
                predicate = { false },
            )

            sut.items() shouldContainExactly listOf(Item(job = newJob, value = "new"))
        }

    @Test
    fun `given a 'predicate' that matches every item, when 'supersede' is called, then the new job is not cancelled`() =
        runTest(testDispatcher) {
            sut = CoroutineRegistry<String>()
            sut.access { add(backgroundScope.launch { awaitCancellation() }, "old") }
            val newJob = backgroundScope.launch { awaitCancellation() }

            sut.supersede(
                job = newJob,
                value = "new",
                predicate = { true },
            )

            newJob.isActive shouldBe true
        }

    @Test
    fun `given a superseded job that takes time to finish cancelling, when 'supersede' returns, then it has not waited for it`() =
        runTest(testDispatcher) {
            sut = CoroutineRegistry<String>()
            val cancellationGate = ClosableSuspendGate(closed = true)
            val supersededJob = backgroundScope.launch {
                try {
                    awaitCancellation()
                } finally {
                    withContext(NonCancellable) { cancellationGate.awaitOpen() }
                }
            }
            sut.access { add(supersededJob, "old") }

            sut.supersede(
                job = backgroundScope.launch { awaitCancellation() },
                value = "new",
                predicate = { it.value == "old" },
            )

            supersededJob.isCompleted shouldBe false
            cancellationGate.open() // let the superseded job finish cancelling
        }

    @Test
    fun `given a superseded coroutine, when it is cancelled by 'supersede', then its parent scope stays active`() =
        runTest(testDispatcher) {
            sut = CoroutineRegistry<String>()
            val parentScope = CoroutineScope(Job() + testDispatcher)
            sut.access {
                val job = parentScope.launch { awaitCancellation() }
                add(job, "old")
            }

            sut.supersede(
                job = parentScope.launch { awaitCancellation() },
                value = "new",
                predicate = { it.value == "old" },
            )

            parentScope.isActive shouldBe true
            parentScope.cancel()
        }

    /**
     * Cancelling a superseded job invokes its completion handlers, which may mutate the registry.
     * [CoroutineRegistry] requires such cancellation to happen outside of the critical section: the lock
     * is reentrant, so a handler invoked under it would let its mutation through in the middle of the
     * critical section instead of being blocked until the section ends.
     */
    @Test
    fun `given a superseded job, when its cancellation is handled, then the lock is not held`() =
        runTest(testDispatcher) {
            sut = CoroutineRegistry<String>()
            val supersededJob = backgroundScope.launch { awaitCancellation() }
            sut.access { add(supersededJob, "old") }
            var wasAccessibleFromAnotherThread: Boolean? = null
            supersededJob.invokeOnCompletion {
                wasAccessibleFromAnotherThread = sut.isAccessibleFromAnotherThread()
            }

            sut.supersede(
                job = backgroundScope.launch { awaitCancellation() },
                value = "new",
                predicate = { it.value == "old" },
            )

            wasAccessibleFromAnotherThread shouldBe true
        }

    @Test
    fun `given a job registered by 'supersede', when it completes, then its item is removed from the registry`() =
        runTest(testDispatcher) {
            sut = CoroutineRegistry<String>()
            val job = Job()
            sut.supersede(
                job = job,
                value = "new",
                predicate = { false },
            )

            job.complete()

            sut.items().shouldBeEmpty()
        }

    /**
     * Tests that matching the items and registering the new one are atomic with respect to each other:
     * if they were not, concurrent calls could miss each other, and more than one item would be left registered.
     */
    @RepeatedTest(10) // executed sequentially (by default)
    fun `given concurrent 'supersede' calls that match each other, when all of them return, then exactly one item is left registered`() =
        runTest(testDispatcher) {
            sut = CoroutineRegistry<String>()
            val numberOfThreads = 16
            val dispatcher = Executors.newFixedThreadPool(numberOfThreads).asCoroutineDispatcher()
            val barrier = CyclicBarrier(numberOfThreads)

            dispatcher.use { dispatcher ->
                coroutineScope {
                    repeat(numberOfThreads) { index ->
                        launch(dispatcher) {
                            barrier.await()
                            sut.supersede(
                                job = Job(),
                                value = "value $index",
                                predicate = { true },
                            )
                        }
                    }
                }
            }

            sut.items() shouldHaveSize 1
        }

    // endregion

    // region 'launchSuperseding'

    @Test
    fun `when a coroutine is launched, then it is registered with its value before its block starts`() =
        runTest(testDispatcher) {
            sut = CoroutineRegistry<String>()
            var valuesObservedByBlock: List<String>? = null

            backgroundScope.launchSuperseding(
                registry = sut,
                value = "1",
                predicate = { false },
            ) {
                valuesObservedByBlock = sut.items().map { it.value }
            }

            valuesObservedByBlock shouldContainExactly listOf("1")
        }

    @Test
    fun `given a running coroutine of the same family, when a new one is launched, then its block does not start until the old one has finished cancelling`() =
        runTest(testDispatcher) {
            sut = CoroutineRegistry<String>()
            val gateOfCancellationOfOldCoroutine = ClosableSuspendGate(closed = true)
            var hasOldFinishedCancelling = false
            backgroundScope.launchSuperseding(
                registry = sut,
                value = "old",
                predicate = { true },
            ) {
                try {
                    awaitCancellation()
                } finally {
                    withContext(NonCancellable) {
                        gateOfCancellationOfOldCoroutine.awaitOpen()
                        hasOldFinishedCancelling = true
                    }
                }
            }

            var hadOldFinishedCancellingWhenNewStarted: Boolean? = null
            backgroundScope.launchSuperseding(
                registry = sut,
                value = "new",
                predicate = { true },
            ) {
                hadOldFinishedCancellingWhenNewStarted = hasOldFinishedCancelling
            }

            hadOldFinishedCancellingWhenNewStarted shouldBe null // the new block is still waiting
            gateOfCancellationOfOldCoroutine.open()
            testDispatcher.scheduler.advanceUntilIdle()
            hadOldFinishedCancellingWhenNewStarted shouldBe true
        }

    @Test
    fun `given a coroutine that is waiting for the superseded ones, when it is superseded itself, then its block is never executed`() =
        runTest(testDispatcher) {
            // GIVEN
            sut = CoroutineRegistry<String>()
            val gateOfCancellationOfFirstCoroutine = ClosableSuspendGate(closed = true)
            backgroundScope.launchSuperseding(
                registry = sut,
                value = "first",
                predicate = { true },
            ) {
                try {
                    awaitCancellation()
                } finally {
                    withContext(NonCancellable) { gateOfCancellationOfFirstCoroutine.awaitOpen() }
                }
            }

            var hasSecondBlockExecuted = false
            val secondJob = backgroundScope.launchSuperseding(
                registry = sut,
                value = "second",
                predicate = { true },
            ) {
                hasSecondBlockExecuted = true
            }
            // the second coroutine is now waiting for the first one to finish cancelling

            // WHEN
            backgroundScope.launchSuperseding(
                registry = sut,
                value = "third",
                predicate = { true },
            ) {
                // some work
            }
            gateOfCancellationOfFirstCoroutine.open()
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            secondJob.isCancelled shouldBe true
            hasSecondBlockExecuted shouldBe false
        }

    /**
     * A superseded coroutine stays registered until it completes, so the last coroutine of a chain
     * waits for every preceding one, not only for its direct predecessor.
     */
    @Test
    fun `given a chain of superseding coroutines, when the last one starts its block, then every preceding coroutine has finished cancelling`() =
        runTest(testDispatcher) {
            // GIVEN
            sut = CoroutineRegistry<String>()
            val gateOfCancellationOfFirstCoroutine = ClosableSuspendGate(closed = true)
            var hasFirstFinishedCancelling = false
            backgroundScope.launchSuperseding(
                registry = sut,
                value = "first",
                predicate = { true },
            ) {
                try {
                    awaitCancellation()
                } finally {
                    withContext(NonCancellable) {
                        gateOfCancellationOfFirstCoroutine.awaitOpen()
                        hasFirstFinishedCancelling = true
                    }
                }
            }

            backgroundScope.launchSuperseding(
                registry = sut,
                value = "second",
                predicate = { true },
            ) {
                // never executed: superseded while it is waiting for the first coroutine
            }

            // WHEN
            var hadFirstFinishedCancellingWhenThirdStarted: Boolean? = null
            backgroundScope.launchSuperseding(
                registry = sut,
                value = "third",
                predicate = { true },
            ) {
                hadFirstFinishedCancellingWhenThirdStarted = hasFirstFinishedCancelling
            }

            // THEN
            hadFirstFinishedCancellingWhenThirdStarted shouldBe null // the third block is still waiting
            gateOfCancellationOfFirstCoroutine.open()
            testDispatcher.scheduler.advanceUntilIdle()
            hadFirstFinishedCancellingWhenThirdStarted shouldBe true
        }

    /**
     * Same as the chain above, but the second coroutine is superseded before its first dispatch,
     * so its body never runs and it never starts waiting for the first coroutine.
     */
    @Test
    fun `given a coroutine that is superseded before it starts, when the next one starts its block, then every preceding coroutine has finished cancelling`() =
        runTest(StandardTestDispatcher()) {
            // GIVEN
            sut = CoroutineRegistry<String>()
            // not 'backgroundScope': 'advanceUntilIdle()' doesn't run its work
            val scope = CoroutineScope(StandardTestDispatcher(testScheduler) + Job())
            val gateOfCancellationOfFirstCoroutine = ClosableSuspendGate(closed = true)
            var hasFirstStarted = false
            var hasFirstFinishedCancelling = false
            scope.launchSuperseding(
                registry = sut,
                value = "first",
                predicate = { true },
            ) {
                hasFirstStarted = true
                try {
                    awaitCancellation()
                } finally {
                    withContext(NonCancellable) {
                        gateOfCancellationOfFirstCoroutine.awaitOpen()
                        hasFirstFinishedCancelling = true
                    }
                }
            }
            runCurrent()
            hasFirstStarted shouldBe true

            var hasSecondBlockExecuted = false
            val secondJob = scope.launchSuperseding(
                registry = sut,
                value = "second",
                predicate = { true },
            ) {
                hasSecondBlockExecuted = true
            }
            // the second coroutine is launched, but not dispatched yet

            // WHEN
            var hadFirstFinishedCancellingWhenThirdStarted: Boolean? = null
            scope.launchSuperseding(
                registry = sut,
                value = "third",
                predicate = { true },
            ) {
                hadFirstFinishedCancellingWhenThirdStarted = hasFirstFinishedCancelling
            }
            runCurrent() // the second coroutine is dispatched only now, already canceled

            // THEN
            secondJob.isCancelled shouldBe true
            hasSecondBlockExecuted shouldBe false
            hadFirstFinishedCancellingWhenThirdStarted shouldBe null // the third block is still waiting
            gateOfCancellationOfFirstCoroutine.open()
            advanceUntilIdle()
            hadFirstFinishedCancellingWhenThirdStarted shouldBe true
            scope.cancel()
        }

    @Test
    fun `given a coroutine that is waiting for the superseded ones, when it is cancelled, then its block is never executed and its item is removed`() =
        runTest(testDispatcher) {
            // GIVEN
            sut = CoroutineRegistry<String>()
            val gateOfCancellationOfOldCoroutine = ClosableSuspendGate(closed = true)
            backgroundScope.launchSuperseding(
                registry = sut,
                value = "old",
                predicate = { true },
            ) {
                try {
                    awaitCancellation()
                } finally {
                    withContext(NonCancellable) { gateOfCancellationOfOldCoroutine.awaitOpen() }
                }
            }

            var hasNewBlockExecuted = false
            val newJob = backgroundScope.launchSuperseding(
                registry = sut,
                value = "new",
                predicate = { true },
            ) {
                hasNewBlockExecuted = true
            }

            // WHEN
            newJob.cancel()
            gateOfCancellationOfOldCoroutine.open()
            testDispatcher.scheduler.advanceUntilIdle()

            // THEN
            hasNewBlockExecuted shouldBe false
            sut.items().shouldBeEmpty()
        }

    @Test
    fun `given a 'predicate' that matches every item, when a coroutine is launched, then its block is executed`() =
        runTest(testDispatcher) {
            sut = CoroutineRegistry<String>()
            var hasBlockExecuted = false

            // if the coroutine supersedes itself, then it waits for itself and the test fails on a timeout
            val job = backgroundScope.launchSuperseding(
                registry = sut,
                value = "1",
                predicate = { true },
            ) {
                hasBlockExecuted = true
            }
            job.join()

            hasBlockExecuted shouldBe true
        }

    @Test
    fun `given a running coroutine of another family, when a new one is launched, then it is not cancelled`() =
        runTest(testDispatcher) {
            sut = CoroutineRegistry<String>()
            val jobOfAnotherFamily = backgroundScope.launchSuperseding(
                registry = sut,
                value = "another family",
                predicate = { false },
            ) {
                awaitCancellation()
            }

            backgroundScope.launchSuperseding(
                registry = sut,
                value = "new",
                predicate = { it.value == "new" },
            ) {
                // some work
            }

            jobOfAnotherFamily.isActive shouldBe true
        }

    @Test
    fun `given a launched coroutine, when its block completes, then its item is removed from the registry`() =
        runTest(testDispatcher) {
            sut = CoroutineRegistry<String>()

            val job = backgroundScope.launchSuperseding(
                registry = sut,
                value = "1",
                predicate = { false },
            ) {
                // some work
            }
            job.join()

            sut.items().shouldBeEmpty()
        }

    @Test
    fun `given a launched coroutine, when its block throws, then its item is removed from the registry`() =
        runTest(testDispatcher) {
            sut = CoroutineRegistry<String>()
            val scope = CoroutineScope(SupervisorJob() + testDispatcher + CoroutineExceptionHandler { _, _ -> })

            val job = scope.launchSuperseding(
                registry = sut,
                value = "1",
                predicate = { false },
            ) {
                error("exception")
            }
            job.join()

            sut.items().shouldBeEmpty()
            scope.cancel()
        }

    @Test
    fun `given a coroutine launched as a child of another registered coroutine, when the parent is superseded, then the child is cancelled and removed from the registry`() =
        runTest(testDispatcher) {
            sut = CoroutineRegistry<String>()
            val gateOfChildCoroutine = ClosableSuspendGate(closed = true)
            lateinit var childJob: Job
            backgroundScope.launchSuperseding(
                registry = sut,
                value = "parent",
                predicate = { it.value == "parent" },
            ) {
                childJob = launchSuperseding(
                    registry = sut,
                    value = "child",
                    predicate = { it.value == "child" },
                    block = { gateOfChildCoroutine.awaitOpen() },
                )
            }

            backgroundScope.launchSuperseding(
                registry = sut,
                value = "parent",
                predicate = { it.value == "parent" },
            ) {
                // some work
            }

            childJob.isCancelled shouldBe true
            sut.items().none { it.value == "child" } shouldBe true
        }

    /**
     * Tests the central guarantee of [launchSuperseding]: at any time there is at most one running block
     * of the family selected by the predicate.
     */
    @RepeatedTest(10) // executed sequentially (by default)
    fun `given many coroutines of the same family launched concurrently, then at most one block is executing at any time`() =
        runTest(testDispatcher) {
            sut = CoroutineRegistry<String>()
            val numberOfCoroutines = 16
            val dispatcher = Executors.newFixedThreadPool(numberOfCoroutines).asCoroutineDispatcher()
            val scope = CoroutineScope(dispatcher)
            val barrier = CyclicBarrier(numberOfCoroutines)
            // atomics to have accurate values in case if test fails and the blocks are executed concurrently
            val activeConcurrentBlocks = AtomicInteger(0)
            val maxConcurrentBlocks = AtomicInteger(0)

            try {
                val launchedJobs = List(numberOfCoroutines) { index ->
                    scope.async {
                        barrier.await()
                        scope.launchSuperseding(
                            registry = sut,
                            value = "value $index",
                            predicate = { true },
                        ) {
                            val active = activeConcurrentBlocks.incrementAndGet()
                            maxConcurrentBlocks.updateAndGet { max(it, active) }
                            try {
                                // widen the time window of the block to give other coroutines a bigger chance to execute theirs concurrently
                                delay(5.milliseconds)
                            } finally {
                                // in 'finally', because the block may be cancelled while it is delaying
                                activeConcurrentBlocks.decrementAndGet()
                            }
                        }
                    }
                }.awaitAll()
                launchedJobs.joinAll()
            } finally {
                scope.cancel()
                dispatcher.close()
            }

            activeConcurrentBlocks.get() shouldBe 0
            maxConcurrentBlocks.get() shouldBe 1
        }

    // endregion
}
