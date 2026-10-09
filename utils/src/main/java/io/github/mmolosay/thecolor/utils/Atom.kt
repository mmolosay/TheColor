package io.github.mmolosay.thecolor.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * A value that can be read and updated atomically.
 *
 * Where the value is stored is not specified: it may be held directly or derived from another value,
 * in which case it also changes whenever that value does.
 *
 * All parts of one change must be made in a single [update], so that no one observes it half-applied.
 * The `transform` must derive the new value from the value it receives, not from [value] read earlier:
 * a change made in between would be lost.
 * The `transform` may be invoked more than once, so it must be free of side effects.
 *
 * Named after Clojure's [atom](https://clojure.org/reference/atoms), which is read and updated the same way.
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