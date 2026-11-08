package io.github.mmolosay.thecolor.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration

/**
 * Produces the most recently [offer]ed value at most once per [period].
 * Invokes [onSampleProduced] callback when a new sample is produced.
 *
 * Thread-safe.
 * Sampling runs in a new coroutine launched in the [coroutineScope] and stops
 * automatically when no new values are offered.
 *
 * The [onSampleProduced] callback is never invoked concurrently.
 * Because the callback runs inside the sampling coroutine, long-running suspending
 * operations inside [onSampleProduced] will delay subsequent sampling.
 */
class Sampler<T>(
    private val period: Duration,
    private val coroutineScope: CoroutineScope,
    private val onSampleProduced: OnSampleProducedCallback<T>,
) {

    private var job: Job? = null
    private var lastValue: Optional<T> = Optional.None
    private val lock = ReentrantLock()

    fun offer(value: T) =
        lock.withLock {
            lastValue = Optional.Value(value)
            if (job?.isActive != true) {
                launchCoroutine()
            }
        }

    private fun launchCoroutine() {
        require(job?.isActive != true)
        coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
            while (true) {
                val sample = lock.withLock {
                    val current = lastValue
                    lastValue = Optional.None
                    if (current is Optional.Value) return@withLock current.value
                    job = null // coroutine will finish due to the following 'return', but Job.isActive may still be 'true'
                    return@launch
                }
                onSampleProduced(sample)
                delay(period)
            }
        }.also { job ->
            this.job = job // will be reached after the coroutine first suspends, and it's OK
        }
    }

    fun interface OnSampleProducedCallback<T> {
        suspend operator fun invoke(value: T)
    }

    /*
     * Type T may be nullable, with 'null' being a valid value.
     * In such cases, we can't distinguish between states of "value present" and "value absent" based on the 'null' value.
     */
    private sealed interface Optional<out T> {
        data object None : Optional<Nothing>
        data class Value<T>(val value: T) : Optional<T>
    }
}

fun <T> Sampler(
    period: Duration,
    coroutineScope: CoroutineScope,
    inputFlow: Flow<T>,
    outputFlow: MutableSharedFlow<T>,
): Sampler<T> {
    val sampler = Sampler<T>(
        period = period,
        coroutineScope = coroutineScope,
    ) { sample -> outputFlow.emit(sample) }
    inputFlow
        .onEach { value -> sampler.offer(value) }
        .launchIn(coroutineScope)
    return sampler
}