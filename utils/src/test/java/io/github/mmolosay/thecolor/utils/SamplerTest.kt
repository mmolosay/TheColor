package io.github.mmolosay.thecolor.utils

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class SamplerTest {

    val testDispatcher = StandardTestDispatcher()
    val coroutineScope = CoroutineScope(testDispatcher)

    lateinit var sut: Sampler<Int>

    @Test
    fun `given SUT is created, when first value is offered, then the sample is produced immediately`() =
        runTest(testDispatcher) {
            val producedSamples = mutableListOf<Int>()
            sut = Sampler(
                period = 100.milliseconds,
                onSampleProduced = { sample -> producedSamples += sample },
                coroutineScope = coroutineScope,
            )

            sut.offer(0)

            producedSamples shouldContainExactly listOf(0)
        }

    // TODO: add KDoc with steps or meaningful test name
    @Test
    fun `#1`() =
        runTest(testDispatcher) {
            val producedSamples = mutableListOf<ValueWithTimeMark<Int>>()
            sut = Sampler(
                period = 100.milliseconds,
                onSampleProduced = { sample ->
                    producedSamples += ValueWithTimeMark(
                        value = sample,
                        timeMark = testScheduler.timeSource.markNow(),
                    )
                },
                coroutineScope = coroutineScope,
            )

            producedSamples.clear()
            sut.offer(0)
            producedSamples shouldHaveSize 1
            producedSamples.single().value shouldBe 0

            producedSamples.clear()
            advanceTimeBy(50.milliseconds)
            sut.offer(1)
            producedSamples shouldHaveSize 0

            advanceUntilIdle()
            producedSamples shouldHaveSize 1
            producedSamples.single().value shouldBe 1
            producedSamples.single().timeMark.elapsedNow() shouldBe 100.milliseconds
        }

    /**
     * Tests that the access to the [Sampler.offer] is synchronized,
     * and thus only one coroutine is launched if multiple threads enter the method concurrently.
     */
    /*
     * The first repetition of the test often passes even if the SUT is broken.
     * This happens because JVM JIT warmup and thread pool initialization reduce true parallelism,
     * making the race condition less likely to manifest.
     * See: JVM warmup, JIT compilation, and probabilistic nature of race conditions.
     */
    @RepeatedTest(10) // executed sequentially (by default)
    fun `given SUT is created, when multiple threads call offer() concurrently, then only one sample is produced`() =
        runTest(testDispatcher) {
            val samplesCount = AtomicInteger(0)
            sut = Sampler(
                period = 100.milliseconds,
                onSampleProduced = { samplesCount.incrementAndGet() },
                coroutineScope = coroutineScope,
            )

            val numberOfThreads = 32
            val barrier = CyclicBarrier(numberOfThreads)
            val executor = Executors.newFixedThreadPool(numberOfThreads)
            repeat(numberOfThreads) { threadIndex ->
                executor.submit {
                    barrier.await()
                    sut.offer(threadIndex)
                }
            }
            executor.shutdown()
            executor.awaitTermination(1, TimeUnit.SECONDS)
            runCurrent()

            samplesCount.get() shouldBe 1
        }

    data class ValueWithTimeMark<T>(
        val value: T,
        val timeMark: ComparableTimeMark,
    )
}