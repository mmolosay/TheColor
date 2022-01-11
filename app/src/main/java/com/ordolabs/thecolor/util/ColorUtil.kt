package com.ordolabs.thecolor.util

// TODO: ColorUtil should be a singleton class, which instance
//  would be injected by DI handler in places of usage.
object ColorUtil {

    private const val HEX_NUMBER_SIGN = '#'

    private val hexFormatRegex = Regex("^#?(?:[0-9a-fA-F]{3}){1,2}\$")

    fun String.colorHexSignless(): String? {
        if (!this.isColorHex()) return null
        if (this.isColorHexSignless()!!) return this
        return this.substring(1)
    }

    fun String.isColorHex(): Boolean =
        this.matches(hexFormatRegex)

    fun String.isColorHexSignless(): Boolean? {
        if (!this.isColorHex()) return null
        return (this.first() != HEX_NUMBER_SIGN)
    }

    fun String.isColorHexSigned(): Boolean? =
        this.isColorHexSignless()?.let { !it }
}