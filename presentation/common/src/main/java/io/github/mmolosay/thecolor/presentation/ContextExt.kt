package io.github.mmolosay.thecolor.presentation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

fun Context.findActivityContext(): Activity {
    if (this is Activity) return this
    if (this is ContextWrapper) {
        val wrapped = this.baseContext
        return wrapped.findActivityContext()
    }
    error("This context doesn't belong to Activity")
}