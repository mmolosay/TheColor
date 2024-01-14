package io.github.mmolosay.main

import com.ordolabs.data.repository.ColorRemoteRepositoryImpl
import com.ordolabs.data.repository.ColorValidatorRepositoryImpl
import com.ordolabs.data.repository.ColorsHistoryRepositoryImpl
import com.ordolabs.data_remote.api.TheColorApiService
import com.ordolabs.domain.repository.ColorRemoteRepository
import com.ordolabs.domain.repository.ColorValidatorRepository
import com.ordolabs.domain.repository.ColorsHistoryRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.migration.DisableInstallInCheck
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
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(THE_COLOR_API_BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

    @Provides
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(makeHttpLoggingInterceptor())
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
    fun bindColorRemoteRepository(impl: ColorRemoteRepositoryImpl): ColorRemoteRepository

    @Binds
    fun bindColorsHistoryRepository(impl: ColorsHistoryRepositoryImpl): ColorsHistoryRepository

    @Binds
    fun bindColorValidatorRepository(impl: ColorValidatorRepositoryImpl): ColorValidatorRepository
}