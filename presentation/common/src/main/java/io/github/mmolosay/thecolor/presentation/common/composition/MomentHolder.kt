package io.github.mmolosay.thecolor.presentation.common.composition

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import io.github.mmolosay.thecolor.composition.Moment
import kotlinx.coroutines.flow.Flow

interface MomentHolder<S> {
    val state: State<S>
    infix fun offer(moment: Moment<S>)
}

internal class MomentHolderImpl<S>(initial: Moment<S>) : MomentHolder<S> {

    private var version = initial.version

    private val _state = mutableStateOf(value = initial.state, policy = structuralEqualityPolicy())
    override val state: State<S> = _state

    override fun offer(moment: Moment<S>) {
        if (moment.version <= version) {
            return
        }
        version = moment.version
        _state.value = moment.state
    }
}

@Composable
fun <S> rememberMomentHolder(
    initial: Moment<S>,
    moments: Flow<Moment<S>>,
): MomentHolder<S> {
    val holder: MomentHolder<S> = remember(moments) {
        MomentHolderImpl(initial)
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(holder, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(state = Lifecycle.State.STARTED) {
            moments.collect { moment ->
                holder.offer(moment)
            }
        }
    }
    return holder
}