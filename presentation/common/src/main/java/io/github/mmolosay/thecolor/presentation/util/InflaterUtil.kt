package io.github.mmolosay.thecolor.presentation.util

import android.view.LayoutInflater
import android.view.View

object InflaterUtil {

    fun LayoutInflater.cloneInViewContext(view: View?): LayoutInflater {
        view ?: return this
        return this.cloneInContext(view.context)
    }
}