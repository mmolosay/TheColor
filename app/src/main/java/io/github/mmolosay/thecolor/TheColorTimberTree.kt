package io.github.mmolosay.thecolor

import timber.log.Timber

class TheColorTimberTree : Timber.DebugTree() {

    // ClassName::methodName
    override fun createStackElementTag(element: StackTraceElement): String {
        val parentTag = super.createStackElementTag(element) // class name
        val methodName = element.methodName
        val tag = StringBuilder().apply {
            append(parentTag)
            if (methodName.isNotEmpty()) {
                append("::")
                append(methodName)
            }
        }.toString()
        return tag
    }
}