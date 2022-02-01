package com.ordolabs.di.module.data

import com.ordolabs.data.repository.ColorRemoteRepository
import com.ordolabs.data.repository.ColorValidatorRepository
import com.ordolabs.data.repository.ColorsHistoryRepository
import com.ordolabs.data_local.dao.ColorsHistoryDao
import com.ordolabs.data_remote.api.TheColorApiService
import com.ordolabs.domain.repository.IColorRemoteRepository
import com.ordolabs.domain.repository.IColorValidatorRepository
import com.ordolabs.domain.repository.IColorsHistoryRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DataRepositoryModule {

    @Provides
    @Singleton
    fun provideColorValidatorRepository(): IColorValidatorRepository =
        ColorValidatorRepository()

    @Provides
    @Singleton
    fun provideColorHistoryRepository(
        dao: ColorsHistoryDao
    ): IColorsHistoryRepository =
        ColorsHistoryRepository(dao)

    @Provides
    @Singleton
    fun provideColorRemoteRepository(
        api: TheColorApiService
    ): IColorRemoteRepository =
        ColorRemoteRepository(api)
}