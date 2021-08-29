package com.ordolabs.thecolor.util

import com.ordolabs.thecolor.util.struct.Resource
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

@Suppress("FunctionName")
fun <V : Any> MutableSharedResourceFlow() =
    MutableSharedFlow<Resource<V>>(
        replay = 1,
        extraBufferCapacity = 0,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

@Suppress("FunctionName")
fun <V : Any> MutableStateResourceFlow(value: Resource<V>) =
    MutableStateFlow(value)