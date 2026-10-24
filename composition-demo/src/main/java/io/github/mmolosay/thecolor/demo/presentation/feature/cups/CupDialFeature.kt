package io.github.mmolosay.thecolor.demo.presentation.feature.cups

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.composition.CompositionUtils.launchAwaited
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.demo.domain.Amount
import io.github.mmolosay.thecolor.demo.domain.CupSampler
import io.github.mmolosay.thecolor.main.di.qualifiers.CoroutineDispatcherDiQualifiers.DefaultDispatcher
import io.github.mmolosay.thecolor.utils.mapState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

class CupDialFeature @AssistedInject constructor(
    @Assisted scope: CoroutineScope,
    @Assisted private val store: Store<CupDialState, *>,
    @Assisted private val environment: CupDialEnvironment,
    // from dependency injection: only the work uses them
    private val sampler: CupSampler,
    @DefaultDispatcher private val workDispatcher: CoroutineDispatcher,
) {

    val viewData: StateFlow<CupDialViewData> =
        store.mapState { dial -> context(environment) { dial.toViewData() } }

    init {
        // starts what the state awaits, and stops what it no longer awaits
        scope.launchAwaited(
            store = store,
            awaited = { dial -> context(environment) { dial.awaited() } },
            block = { request ->
                // off the main thread, as sampling a color area would be; the Views collect on the
                // main thread either way
                withContext(workDispatcher) {
                    val amount = sampler.amountAt(request.position)
                    store.update { dial ->
                        context(environment) { dial.receiveSample(request, amount) }
                    }
                }
            },
        )
    }

    fun drag(position: Float) {
        store.update { dial ->
            context(environment) { dial.drag(position) }
        }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            scope: CoroutineScope,
            store: Store<CupDialState, *>,
            environment: CupDialEnvironment,
        ): CupDialFeature
    }
}

// The transitions behind the actions and the results. Only the feature instance calls them. They're
// outside the class so that they can't reach its scope or store: they can run more than once, so
// they must have no side effects

// the dial follows the finger at once; a sample starts unless one is being worked out already
context(_: CupDialEnvironment)
fun CupDialState.drag(position: Float): Change<CupDialState, Nothing> {
    val clamped = position.coerceIn(0f, MaxCups)
    if (clamped == this.position) return Change(this)
    val moved = this.withPosition(clamped)
    val request = moved.awaited() ?: run {
        CupSampleRequest(position = clamped, amountVersion = moved.amountVersion)
    }
    val state = moved.copy(request = request)
    return Change(state)
}

// Applies only to the sample the state still waits for. If the dial has moved on since the sample
// started, another one starts, for where the dial is now
context(_: CupDialEnvironment)
fun CupDialState.receiveSample(
    request: CupSampleRequest,
    amount: Amount.Cups,
): Change<CupDialState, Nothing> {
    if (this.awaited() != request) return Change(this) // the amount was set from elsewhere since
    val sampled = this.withAmountOnly(amount)
    val next = run {
        if (sampled.position == request.position) return@run null
        CupSampleRequest(position = sampled.position, amountVersion = sampled.amountVersion)
    }
    val state = sampled.copy(request = next)
    return Change(state)
}
