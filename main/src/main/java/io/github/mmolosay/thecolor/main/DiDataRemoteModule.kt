package io.github.mmolosay.thecolor.main

import com.squareup.moshi.Moshi
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.migration.DisableInstallInCheck
import io.github.mmolosay.thecolor.data.remote.ColorRepositoryRemoteImpl
import io.github.mmolosay.thecolor.data.remote.HttpFailureFactoryImpl
import io.github.mmolosay.thecolor.data.remote.api.TheColorApiService
import io.github.mmolosay.thecolor.data.remote.model.SchemeModeDtoAdapter
import io.github.mmolosay.thecolor.domain.color.ColorRepository
import io.github.mmolosay.thecolor.domain.dev.options.DefaultDevOptions
import io.github.mmolosay.thecolor.domain.dev.options.DevOptions
import io.github.mmolosay.thecolor.domain.dev.options.DevOptionsRepository
import io.github.mmolosay.thecolor.domain.dev.options.valueOrElse
import io.github.mmolosay.thecolor.domain.result.HttpFailureFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Part of [DiDataModule].
 * Focuses on data components related to remote sources.
 */
@Module(
    includes = [DiDataRemoteProvideModule::class, DiDataRemoteBindModule::class],
)
@DisableInstallInCheck
object DiDataRemoteModule

@Module
@DisableInstallInCheck
object DiDataRemoteProvideModule {

    @Provides
    @Singleton
    fun provideTheColorApiService(
        httpLoggingInterceptorFactory: DevOptionsHttpLoggingInterceptor.Factory,
    ): TheColorApiService {
        val okHttpClient = run {
            OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .apply addHttpLoggingInterceptor@{
                    val interceptor = httpLoggingInterceptorFactory.create(
                        delegate = HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BODY
                        },
                    )
                    addInterceptor(interceptor)
                }
                .build()
        }
        val retrofit = run {
            val moshi = Moshi.Builder()
                .add(SchemeModeDtoAdapter())
                .build()
            val moshiConverterFactory = MoshiConverterFactory.create(moshi)
            Retrofit.Builder()
                .baseUrl("https://www.thecolorapi.com/")
                .client(okHttpClient)
                .addConverterFactory(moshiConverterFactory)
                .build()
        }
        return retrofit.create(TheColorApiService::class.java)
    }
}

@Module
@DisableInstallInCheck
interface DiDataRemoteBindModule {

    @Binds
    fun bindHttpFailureFactory(impl: HttpFailureFactoryImpl): HttpFailureFactory

    @Binds
    fun bindColorRepositoryRemoteImpl(impl: ColorRepositoryRemoteImpl): ColorRepository
}

/**
 * A [HttpLoggingInterceptor] that adheres to the [DevOptions.HttpLogging] feature.
 */
class DevOptionsHttpLoggingInterceptor @AssistedInject constructor(
    @Assisted private val delegate: HttpLoggingInterceptor,
    private val devOptionsRepository: DevOptionsRepository,
    private val defaultDevOptions: DefaultDevOptions,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val isHttpLoggingEnabled = devOptionsRepository.flowOfHttpLogging
            .value.valueOrElse { defaultDevOptions.httpLogging }
            .enabled
        return if (isHttpLoggingEnabled) {
            delegate.intercept(chain)
        } else {
            chain.proceed(chain.request())
        }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            delegate: HttpLoggingInterceptor,
        ): DevOptionsHttpLoggingInterceptor
    }
}