package com.ordolabs.di.module.data.remote

import com.ordolabs.data_remote.api.TheColorApiService
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class DataRemoteModule {

    fun inject(): Retrofit =
        provideRetrofit(
            client = makeOkHttpClient()
        )

    @Provides
    @Singleton
    fun provideRetrofit(
        client: OkHttpClient
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(THE_COLOR_API_BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

    private fun makeOkHttpClient(): OkHttpClient {
        val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(httpLoggingInterceptor)
            .build()
    }

    // region Api services

    @Provides
    fun provideTheColorApiService(
        retrofit: Retrofit
    ): TheColorApiService =
        retrofit.create(TheColorApiService::class.java)

    // endregion

    companion object {
        private const val THE_COLOR_API_BASE_URL = "https://www.thecolorapi.com/"
        private const val CONNECT_TIMEOUT_SECONDS = 10L
        private const val READ_TIMEOUT_SECONDS = 10L
    }
}