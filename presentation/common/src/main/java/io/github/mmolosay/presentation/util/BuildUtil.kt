package io.github.mmolosay.presentation.util

import io.github.mmolosay.presentation.common.BuildConfig

object BuildUtil  {

    fun isDebugBuild(): Boolean {
        return BuildConfig.DEBUG
    }
}