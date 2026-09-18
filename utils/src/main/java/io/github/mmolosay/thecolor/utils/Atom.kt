package io.github.mmolosay.thecolor.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * A value that can be read and updated atomically.
 *
 * The value changes only through [update], which is atomic.
 * The contract doesn't specify where the value is stored: it may be held directly or derived from another value.
 *
 * Named after Clojure's atom, which is read and updated the same way.
 */
interface Atom<T> : UpdateScope<T> {
    val value: T
}

fun <S, V> Atom(
    flow: MutableStateFlow<S>,
    lens: Lens<S, V>,
): Atom<V> =
    MutableStateFlowAtom(flow, lens)

private class MutableStateFlowAtom<S, V>(
    private val flow: MutableStateFlow<S>,
    private val lens: Lens<S, V>,
) : Atom<V> {

    override val value: V
        get() = lens.get(flow.value)

    override fun update(transform: (V) -> V) =
        flow.update { s ->
            lens.modify(s, transform)
        }
}