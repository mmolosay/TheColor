package com.ordolabs.thecolor.mapper

import com.ordolabs.domain.model.ColorHex
import com.ordolabs.thecolor.model.ColorHexPresentation
import java.lang.NumberFormatException
import com.github.michaelbull.result.Result
import com.ordolabs.thecolor.util.ext.error
import com.ordolabs.thecolor.util.ext.success

internal fun ColorHexPresentation.toDomain(): Result<ColorHex, Throwable> {
    val valueString = this.value.let {
        if (it.startsWith('#')) it.substring(1) else it
    }
    return try {
        val valueInt = valueString.toInt(16)
        Result.success(ColorHex(value = valueInt))
    } catch (e: NumberFormatException) {
        Result.error(e)
    }
}