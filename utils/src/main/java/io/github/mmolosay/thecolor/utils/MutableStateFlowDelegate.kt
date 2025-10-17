package io.github.mmolosay.thecolor.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * A property delegate that exposes a [MutableStateFlow]'s `value` as a mutable property.
 *
 * The property getter and setter are backed by the underlying [MutableStateFlow].
 *
 * Example:
 * ```
 * val flowOfName = MutableStateFlow<String?>(null)
 * var name by flowOfName.asDelegate()
 * ```
 */
interface MutableStateFlowDelegate<T> : ReadWriteProperty<Any?, T>

fun <T> MutableStateFlow<T>.asDelegate(): MutableStateFlowDelegate<T> =
    MutableStateFlowDelegateImpl(mutableStateFlow = this)

private class MutableStateFlowDelegateImpl<T>(
    private val mutableStateFlow: MutableStateFlow<T>,
) : MutableStateFlowDelegate<T> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): T =
        mutableStateFlow.value

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        mutableStateFlow.value = value
    }
}