package io.github.mmolosay.thecolor.utils

import java.util.concurrent.atomic.AtomicInteger

interface SideEffect {

    val id: Id

    @JvmInline
    value class Id(val int: Int)
}

class SideEffectIdFactory {
    private val nextInt = AtomicInteger(0)

    fun get(): SideEffect.Id {
        val int = nextInt.getAndIncrement()
        return SideEffect.Id(int)
    }
}