package io.github.mmolosay.thecolor.main

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.migration.DisableInstallInCheck
import io.github.mmolosay.thecolor.data.remote.ColorRepositoryRemoteImpl
import io.github.mmolosay.thecolor.data.remote.HttpFailureFactoryImpl
import io.github.mmolosay.thecolor.data.remote.api.TheColorApiService
import io.github.mmolosay.thecolor.data.remote.model.SchemeModeDtoAdapter
import io.github.mmolosay.thecolor.domain.repository.ColorRepository
import io.github.mmolosay.thecolor.domain.usecase.HttpFailureFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

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
    fun provideTheColorApiService(
        retrofit: Retrofit,
    ): TheColorApiService =
        retrofit.create(TheColorApiService::class.java)

    @Provides
    fun provideRetrofit(
        client: OkHttpClient,
    ): Retrofit {
        val moshi = makeMoshi()
        val moshiConverterFactory = MoshiConverterFactory.create(moshi)
        return Retrofit.Builder()
            .baseUrl(THE_COLOR_API_BASE_URL)
            .client(client)
            .addConverterFactory(moshiConverterFactory)
            .build()
    }

    @Provides
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(makeHttpLoggingInterceptor())
            .build()

    private fun makeMoshi(): Moshi =
        Moshi.Builder()
            .add(SchemeModeDtoAdapter())
            .addLast(KotlinJsonAdapterFactory())
            .build()

    private fun makeHttpLoggingInterceptor() =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    private const val THE_COLOR_API_BASE_URL = "https://www.thecolorapi.com/"
    private const val CONNECT_TIMEOUT_SECONDS = 10L
    private const val READ_TIMEOUT_SECONDS = 10L
}

@Module
@DisableInstallInCheck
interface DiDataRemoteBindModule {

    @Binds
    fun bindHttpFailureFactory(impl: HttpFailureFactoryImpl): HttpFailureFactory

    @Binds
    fun bindColorRepositoryRemoteImpl(impl: ColorRepositoryRemoteImpl): ColorRepository
}