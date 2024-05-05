package io.github.mmolosay.thecolor.data.remote

import io.github.mmolosay.thecolor.domain.failure.HttpFailure
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

internal fun Throwable.asHttpFailureOrNull(): HttpFailure? =
    when (this) {
        is UnknownHostException -> HttpFailure.UnknownHost(cause = this)
        is SocketTimeoutException -> HttpFailure.Timeout(cause = this)
        is IOException -> HttpFailure.IO(cause = this)
        is HttpException -> HttpFailure.ErrorResponse(
            cause = this,
            code = this.code(),
            message = this.message(),
        )
        else -> null
    }