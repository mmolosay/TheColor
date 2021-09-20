package com.ordolabs.thecolor.util

import com.ordolabs.thecolor.util.struct.Resource
import com.ordolabs.thecolor.util.struct.success
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

@Suppress("FunctionName")
fun <V : Any> MutableSharedResourceFlow() =
    MutableSharedFlow<Resource<V>>()

@Suppress("FunctionName")
fun <V : Any> MutableStateResourceFlow(value: Resource<V>) =
    MutableStateFlow(value)

@Suppress("FunctionName")
fun <V : Any> MutableStateResourceFlow(value: V) =
    MutableStateFlow(Resource.success(value))