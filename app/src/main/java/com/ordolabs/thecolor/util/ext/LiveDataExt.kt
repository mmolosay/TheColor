package com.ordolabs.thecolor.util.ext

import androidx.lifecycle.MutableLiveData
import com.ordolabs.thecolor.util.struct.Resource

fun <V> MutableLiveData<Resource<V>>.setLoading() {
    this.value = Resource.loading()
}

fun <V : Any> MutableLiveData<Resource<V>>.setSuccess(value: V) {
    this.value = Resource.success(value)
}

fun <V : Any> MutableLiveData<Resource<V>>.setFailure(value: V) {
    this.value = Resource.failure(value)
}