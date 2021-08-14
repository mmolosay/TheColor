package com.ordolabs.thecolor.util

import com.ordolabs.thecolor.BuildConfig

object BuildUtil  {

    fun isDebugBuild(): Boolean {
        return BuildConfig.DEBUG
    }
}