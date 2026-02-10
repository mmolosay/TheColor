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
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration

class Sampler<T>(
    private val period: Duration,
    private val onSampleProduced: OnSampleProducedCallback<T>,
    private val coroutineScope: CoroutineScope,
) {

    private var job: Job? = null
    private val lastValue = AtomicReference<Optional<T>>(Optional.None)

    @Synchronized
    fun offer(value: T) {
        lastValue.set(Optional.Value(value))
        if (job?.isActive != true) {
            launchCoroutine()
        }
    }

    private fun launchCoroutine() {
        require(job?.isActive != true)
        coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
            while (true) {
                val sample = lastValue.getAndSet(Optional.None)
                    .let { it as? Optional.Value ?: break }
                    .value
                onSampleProduced(sample)
                delay(period)
            }
        }.also { job ->
            this.job = job // will be reached after the coroutine first suspends, and this is OK
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
    inputFlow: Flow<T>,
    outputFlow: MutableSharedFlow<T>,
    coroutineScope: CoroutineScope,
): Sampler<T> {
    val sampler = Sampler<T>(
        period = period,
        onSampleProduced = { sample -> outputFlow.emit(sample) },
        coroutineScope = coroutineScope,
    )
    inputFlow
        .onEach { value -> sampler.offer(value) }
        .launchIn(coroutineScope)
    return sampler
}