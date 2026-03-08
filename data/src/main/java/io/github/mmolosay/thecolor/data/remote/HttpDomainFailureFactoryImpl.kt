package io.github.mmolosay.thecolor.data.remote

import io.github.mmolosay.thecolor.domain.exception.DomainFailure
import io.github.mmolosay.thecolor.domain.exception.HttpDomainFailureFactory
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

class HttpDomainFailureFactoryImpl @Inject constructor() : HttpDomainFailureFactory {

    override fun Throwable.toHttpDomainFailureOrNull(): DomainFailure.Http? =
        when (this) {
            is UnknownHostException -> DomainFailure.Http.UnknownHost
            is SocketTimeoutException -> DomainFailure.Http.Timeout
            is IOException -> DomainFailure.Http.IO
            is HttpException -> DomainFailure.Http.ErrorResponse(
                httpCode = this.code(),
                httpMessage = this.message(),
            )
            else -> null
        }
}