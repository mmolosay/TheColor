package com.ordolabs.thecolor.util.ext

import com.ordolabs.thecolor.util.struct.Resource
import com.ordolabs.thecolor.util.struct.empty
import com.ordolabs.thecolor.util.struct.loading
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn

/**
 * Converts [Flow] in [SharedFlow] with `replay` parameter equal to __zero__,
 * so it doesn't re-emit last values for new collectors.
 * It is usefull, when you want display some UI error state,
 * but it should not be collected again on UI lifecycle restart, when old collectors
 * are being re-attached to `this` `Flow`.
 *
 * @see Flow.shareIn
 */
fun <T> Flow<T>.shareOnceIn(
    scope: CoroutineScope,
    started: SharingStarted = SharingStarted.Lazily
): SharedFlow<T> =
    this.shareIn(scope, started, replay = 0)

fun <V> MutableStateFlow<Resource<V>>.setEmpty() {
    this.value = this.value.empty()
}

fun <V> MutableStateFlow<Resource<V>>.setLoading() {
    this.value = this.value.loading()
}

fun <V> MutableStateFlow<Resource<V>>.setSuccess(value: V) {
    this.value = Resource.Success(value)
}

///**
// * Updates [MutableStateFlow]<[Resource]<[V]>> atomically using the specified [function] of its value.
// * Due to [StateFlow]'s __strong equality-based conflation__, if current `value` is equal to new
// * value from [function], then `value` is first updated with [Resource].[empty] value.
// */
//fun <V : Any> MutableStateFlow<Resource<V>>.updateGuaranteed(
//    function: (Resource<V>) -> Resource<V>
//) {
//    val oldValue = this.value
//    val newValue = function(oldValue)
//    if (newValue == oldValue) this.update { Resource.empty() }
//    this.update { newValue }
//}