package io.github.mmolosay.thecolor.presentation.input

import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.presentation.input.ColorInputMediator.ColorState
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import java.util.Collections
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.max
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class ColorInputMediatorTest {

    val testDispatcher = UnconfinedTestDispatcher()

    lateinit var sut: ColorInputMediator

    @Test
    fun `when SUT is initialized, then 'colorStateFlow' has correct initial value`() {
        createSut()

        val expectedValue = ColorState(color = null, source = null, revision = ColorState.Revision(0))
        sut.colorState shouldBe expectedValue
    }

    @Test
    fun `when 'null' color is set, then 'colorStateFlow' is updated with 'null' color`() =
        runTest(testDispatcher) {
            createSut()

            sut.set(color = null, source = null, revision = sut.newRevision())

            sut.colorState.color shouldBe null
        }

    @Test
    fun `when not-null color is set, then 'colorStateFlow' is updated with the specified color`() =
        runTest(testDispatcher) {
            createSut()

            val color = Color.Hex(0x0)
            sut.set(color = color, source = null, revision = sut.newRevision())

            sut.colorState.color shouldBe color
        }

    @Test
    fun `when any single update is made via an editor, then 'colorStateFlow' is updated accordingly`() =
        runTest(testDispatcher) {
            createSut()

            val color = Color.Hex(0x0)
            val source = mockk<ColorState.Source>()
            sut.withLock { editor ->
                editor.set(color = color, source = source, revision = sut.newRevision())
            }

            sut.colorState.color shouldBe color
            sut.colorState.source shouldBe source
        }

    @Test
    fun `when any multiple updates are made via an editor, then 'colorStateFlow' is updated accordingly`() =
        runTest(testDispatcher) {
            createSut()

            val color1 = Color.Hex(0x0)
            val source1 = mockk<ColorState.Source>()
            val color2 = Color.Hex(0x1)
            val source2 = mockk<ColorState.Source>()
            color1 shouldNotBe color2
            source1 shouldNotBe source2
            sut.withLock { editor ->
                editor.set(color = color1, source = source1, revision = sut.newRevision())
                editor.set(color = color2, source = source2, revision = sut.newRevision())
            }

            sut.colorState.color shouldBe color2
            sut.colorState.source shouldBe source2
        }

    /**
     * Tests that [ColorInputMediator.withLock] can only be entered from a single thread at a time,
     * thus critical section and editing are synchronized and safe for concurrency.
     */
    @RepeatedTest(10) // executed sequentially (by default)
    fun `when updates are made concurrently, then the critical section is synchronized`() =
        runTest(testDispatcher) {
            createSut()

            val numberOfThreads = 10
            val dispatcher = Executors.newFixedThreadPool(numberOfThreads).asCoroutineDispatcher()
            val barrier = CyclicBarrier(numberOfThreads)
            // atomics to have accurate values in case if test fails and the critical section is not synchronized
            val activeConcurrentExecutions = AtomicInteger(0)
            val maxConcurrentExecutions = AtomicInteger(0)
            suspend fun executeCoroutine() {
                barrier.await() // ensure coroutine has started
                sut.withLock {
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

    /**
     * Tests that [ColorInputMediator.withLock] correctly synchronizes concurrent editing, and
     * all edits are actually executed, which is reflected in updates of [ColorInputMediator.colorStateFlow].
     */
    @RepeatedTest(10) // executed sequentially (by default)
    fun `when updates are made concurrently, then all of them are actually executed`() =
        runTest(testDispatcher) {
            createSut()

            val numberOfThreads = 10
            val dispatcher = Executors.newFixedThreadPool(numberOfThreads).asCoroutineDispatcher()
            val barrier = CyclicBarrier(numberOfThreads)
            val colorStates = Collections.synchronizedList<ColorState>(mutableListOf())
            fun color(index: Int): Color = Color.Hex(index)
            var index = 0 // no need to be thread-safe, will be mutated in synchronized context
            suspend fun executeCoroutine() {
                barrier.await() // ensure coroutine has started
                sut.withLock { editor ->
                    editor.set(color = color(index), revision = sut.newRevision())
                    colorStates += sut.colorState
                    index++
                }
            }
            coroutineScope {
                repeat(numberOfThreads) {
                    launch(dispatcher) {
                        executeCoroutine()
                    }
                }
            }

            val expectedColors = (0 until numberOfThreads).map { color(it) }
            colorStates.map { it.color } shouldContainExactly expectedColors
            colorStates.size shouldBe numberOfThreads
            dispatcher.close()
        }

    @Test
    fun `when edits are made via an editor outside of the 'withLock' block, then an exception is thrown`() =
        runTest(testDispatcher) {
            createSut()

            val capturedEditor: ColorInputMediator.Editor
            sut.withLock { editor ->
                capturedEditor = editor
            }

            shouldThrow<IllegalStateException> {
                capturedEditor.set(color = Color.Hex(0x0), revision = sut.newRevision())
            }
        }

    @Test
    fun `when edits are made via a foreign editor inside the 'withLock' block, then an exception is thrown`() =
        runTest(testDispatcher) {
            createSut()

            val capturedEditor: ColorInputMediator.Editor
            sut.withLock { editor ->
                capturedEditor = editor
            }
            @Suppress("unused")
            sut.withLock { localEditor ->
                shouldThrow<IllegalStateException> {
                    capturedEditor.set(color = Color.Hex(0x0), revision = sut.newRevision())
                }
            }
        }

    @Test
    fun `when new revisions are taken one after another, then each is later than the previous one`() {
        createSut()

        val first = sut.newRevision()
        val second = sut.newRevision()

        second shouldBeGreaterThan first
        first shouldBeGreaterThan sut.colorState.revision
    }

    @RepeatedTest(10) // executed sequentially (by default)
    fun `when new revisions are taken concurrently, then all of them are distinct`() =
        runTest(testDispatcher) {
            createSut()

            val numberOfThreads = 10
            val revisionsPerThread = 100
            val dispatcher = Executors.newFixedThreadPool(numberOfThreads).asCoroutineDispatcher()
            val barrier = CyclicBarrier(numberOfThreads)
            val revisions = Collections.synchronizedList<ColorState.Revision>(mutableListOf())
            coroutineScope {
                repeat(numberOfThreads) {
                    launch(dispatcher) {
                        barrier.await() // ensure coroutines start at the same time
                        repeat(revisionsPerThread) {
                            revisions += sut.newRevision()
                        }
                    }
                }
            }

            revisions.toSet().size shouldBe (numberOfThreads * revisionsPerThread)
            dispatcher.close()
        }

    @Test
    fun `when a change is set with a later revision, then it is applied`() =
        runTest(testDispatcher) {
            createSut()

            val color = Color.Hex(0x0)
            val revision = sut.newRevision()
            val isApplied = sut.set(color = color, source = null, revision = revision)

            isApplied shouldBe true
            sut.colorState shouldBe ColorState(color = color, source = null, revision = revision)
        }

    @Test
    fun `when a change is set with an earlier revision than the current state's, then it is ignored`() =
        runTest(testDispatcher) {
            createSut()
            val earlierRevision = sut.newRevision()
            val laterRevision = sut.newRevision()
            sut.set(color = Color.Hex(0x1), source = null, revision = laterRevision)
            val stateBefore = sut.colorState

            val isApplied = sut.set(color = Color.Hex(0x0), source = null, revision = earlierRevision)

            isApplied shouldBe false
            sut.colorState shouldBe stateBefore
        }

    @Test
    fun `when a change is set with the same revision as the current state's, then it is ignored`() =
        runTest(testDispatcher) {
            createSut()
            val revision = sut.newRevision()
            sut.set(color = Color.Hex(0x1), source = null, revision = revision)
            val stateBefore = sut.colorState

            val isApplied = sut.set(color = Color.Hex(0x0), source = null, revision = revision)

            isApplied shouldBe false
            sut.colorState shouldBe stateBefore
        }

    fun createSut() =
        ColorInputMediator().also {
            sut = it
        }
}