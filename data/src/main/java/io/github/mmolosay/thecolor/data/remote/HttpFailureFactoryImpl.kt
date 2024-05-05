package io.github.mmolosay.thecolor.data.remote

import io.github.mmolosay.thecolor.domain.failure.HttpFailure
import io.github.mmolosay.thecolor.domain.usecase.HttpFailureFactory
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

class HttpFailureFactoryImpl @Inject constructor() : HttpFailureFactory {

    override fun Throwable.asHttpFailureOrNull(): HttpFailure? =
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
}