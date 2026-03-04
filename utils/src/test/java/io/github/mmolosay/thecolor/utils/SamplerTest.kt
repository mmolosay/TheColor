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
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class SamplerTest {

    val testDispatcher = StandardTestDispatcher()
    val coroutineScope = CoroutineScope(testDispatcher)

    lateinit var sut: Sampler<Int?>

    @Test
    fun `given SUT is created, when first value is offered, then the sample is produced immediately`() =
        runTest(testDispatcher) {
            val producedSamples = mutableListOf<Int?>()
            sut = Sampler(
                period = 100.milliseconds,
                coroutineScope = coroutineScope,
            ) { sample -> producedSamples += sample }

            sut.offer(0)

            producedSamples shouldContainExactly listOf(0)
        }

    @Test
    fun `intermediate values within the same period are dropped, and only the last one is produced as sample once period has ended`() =
        runTest(testDispatcher) {
            val producedSamples = mutableListOf<Int?>()
            sut = Sampler(
                period = 100.milliseconds,
                coroutineScope = coroutineScope,
            ) { sample -> producedSamples += sample }

            producedSamples.clear()
            sut.offer(0)
            producedSamples shouldHaveSize 1
            producedSamples.single() shouldBe 0

            producedSamples.clear()
            advanceTimeBy(50.milliseconds)
            sut.offer(1)
            producedSamples shouldHaveSize 0 // no new sample produced

            advanceUntilIdle()
            producedSamples shouldHaveSize 1 // new sample '1' has been produced
            producedSamples.single() shouldBe 1
        }

    @Test
    fun `multiple values spaced beyond the period duration are produced as samples`() =
        runTest(testDispatcher) {
            val producedSamples = mutableListOf<Int?>()
            sut = Sampler(
                period = 100.milliseconds,
                coroutineScope = coroutineScope,
            ) { sample -> producedSamples += sample }

            producedSamples.clear()
            sut.offer(0)
            producedSamples shouldHaveSize 1
            producedSamples.single() shouldBe 0

            producedSamples.clear()
            advanceTimeBy(101.milliseconds)
            sut.offer(1)
            producedSamples shouldHaveSize 1
            producedSamples.single() shouldBe 1

            producedSamples.clear()
            advanceTimeBy(101.milliseconds)
            sut.offer(2)
            producedSamples shouldHaveSize 1
            producedSamples.single() shouldBe 2
        }

    @Test
    fun `when two same values are offered within the same period, then they are produced as samples in individual periods`() =
        runTest(testDispatcher) {
            val producedSamples = mutableListOf<Int?>()
            sut = Sampler(
                period = 100.milliseconds,
                coroutineScope = coroutineScope,
            ) { sample -> producedSamples += sample }

            producedSamples.clear()
            sut.offer(0)
            advanceTimeBy(50.milliseconds)
            sut.offer(0)
            producedSamples shouldHaveSize 1
            producedSamples.single() shouldBe 0

            producedSamples.clear()
            advanceTimeBy(51.milliseconds) // advance to the start of the next period
            producedSamples shouldHaveSize 1
            producedSamples.single() shouldBe 0
        }

    @Test
    fun `offered 'null' values are treated as correct values and produced as samples`() =
        runTest(testDispatcher) {
            val producedSamples = mutableListOf<Int?>()
            sut = Sampler(
                period = 100.milliseconds,
                coroutineScope = coroutineScope,
            ) { sample -> producedSamples += sample }

            producedSamples.clear()
            sut.offer(0)
            producedSamples shouldHaveSize 1
            producedSamples.single() shouldBe 0

            producedSamples.clear()
            advanceTimeBy(101.milliseconds)
            sut.offer(null)
            producedSamples shouldHaveSize 1
            producedSamples.single() shouldBe null
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
                coroutineScope = coroutineScope,
            ) { samplesCount.incrementAndGet() }

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
}