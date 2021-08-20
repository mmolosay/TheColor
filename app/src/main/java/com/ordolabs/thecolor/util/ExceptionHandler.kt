package com.ordolabs.thecolor.util

import androidx.annotation.StringRes
import com.ordolabs.thecolor.R
import java.net.UnknownHostException

internal object ExceptionHandler {

    @StringRes
    fun parseExceptionType(t: Throwable): Int = when (t) {
        is UnknownHostException -> R.string.error_check_internet_connection
        else -> R.string.error_unknown
    }
}