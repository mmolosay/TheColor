package io.github.mmolosay.presentation.viewmodel

import androidx.annotation.StringRes
import io.github.mmolosay.presentation.R
import java.net.UnknownHostException

object ExceptionHandler {

    @StringRes
    fun parseExceptionType(t: Throwable): Int = when (t) {
        is UnknownHostException -> R.string.error_lost_internet_connection
        else -> R.string.error_unhandled
    }
}