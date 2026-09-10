package io.github.mmolosay.thecolor.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Access to a value owned elsewhere: it can be read and atomically updated, but not observed.
 *
 * Lets a component operate on its part of an owner's data without knowing the owner's
 * data type or how that data is stored and published.
 * It's an [UpdateScope] that applies each update immediately.
 *
 * The name is short for "reference": a `Ref` holds no value of its own, only refers to one
 * held elsewhere. It's also the established name for this contract (read and atomic update,
 * no subscribers), as in Cats Effect and ZIO.
 */
interface Ref<T> : UpdateScope<T> {
    val value: T
}

fun <S, V> Ref(
    flow: MutableStateFlow<S>,
    lens: Lens<S, V>,
): Ref<V> =
    MutableStateFlowRef(flow, lens)

private class MutableStateFlowRef<S, V>(
    private val flow: MutableStateFlow<S>,
    private val lens: Lens<S, V>,
) : Ref<V> {

    override val value: V
        get() = lens.get(flow.value)

    override fun update(transform: (V) -> V) =
        flow.update { s ->
            lens.modify(s, transform)
        }
}