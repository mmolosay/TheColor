package io.github.mmolosay.thecolor.utils

interface Lens<S, V> {
    fun get(source: S): V
    fun set(source: S, value: V): S
}

fun <S, V> Lens(
    get: (source: S) -> V,
    set: (source: S, value: V) -> S,
): Lens<S, V> =
    object : Lens<S, V> {
        override fun get(source: S): V = get(source)
        override fun set(source: S, value: V): S = set(source, value)
    }

inline fun <S, V> Lens<S, V>.modify(
    source: S,
    transform: (V) -> V,
): S {
    val value = this.get(source)
    val new = transform(value)
    return this.set(source, new)
}

infix fun <A, B, C> Lens<A, B>.then(next: Lens<B, C>): Lens<A, C> =
    Lens(
        get = { a -> next.get(this.get(a)) },
        set = { a, c -> this.modify(a) { b -> next.set(b, c) } },
    )