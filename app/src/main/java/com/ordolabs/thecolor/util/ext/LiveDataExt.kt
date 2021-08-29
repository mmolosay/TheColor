package com.ordolabs.thecolor.util.ext

import androidx.annotation.MainThread
import androidx.lifecycle.MutableLiveData
import com.ordolabs.thecolor.util.struct.Resource

@MainThread
fun <V> MutableLiveData<Resource<V>>.setLoading() {
    this.value = Resource.loading()
}

@MainThread
fun <V : Any> MutableLiveData<Resource<V>>.setSuccess(value: V) {
    this.value = Resource.success(value)
}

@MainThread
fun <V : Any> MutableLiveData<Resource<V>>.setFailure(value: V) {
    this.value = Resource.failure(value)
}

fun <V> MutableLiveData<Resource<V>>.postLoading() {
    this.postValue(Resource.loading())
}

fun <V : Any> MutableLiveData<Resource<V>>.postSuccess(value: V) {
    this.postValue(Resource.success(value))
}

fun <V : Any> MutableLiveData<Resource<V>>.postFailure(value: V) {
    this.postValue(Resource.failure(value))
}