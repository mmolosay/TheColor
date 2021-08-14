package com.ordolabs.thecolor.util

fun debugError(message: Any) {
    if (BuildUtil.isDebugBuild()) {
        error(message)
    }
}