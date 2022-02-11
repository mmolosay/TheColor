package com.ordolabs.data_bridge.model.repository

import com.ordolabs.data.repository.ColorRemoteRepositoryImpl
import com.ordolabs.data.repository.ColorValidatorRepositoryImpl
import com.ordolabs.data.repository.ColorsHistoryRepositoryImpl
import com.ordolabs.data_local.dao.ColorsHistoryDao
import com.ordolabs.data_remote.api.TheColorApiService
import com.ordolabs.domain.repository.ColorRemoteRepository
import com.ordolabs.domain.repository.ColorValidatorRepository
import com.ordolabs.domain.repository.ColorsHistoryRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(
    includes = [
        DataRepositoryBindModule::class
    ]
)
class DataRepositoryModule {

    @Provides
    @Singleton
    fun provideColorValidatorRepository(): ColorValidatorRepository =
        ColorValidatorRepositoryImpl()

    @Provides
    @Singleton
    fun provideColorsHistoryRepository(
        dao: ColorsHistoryDao
    ): ColorsHistoryRepository =
        ColorsHistoryRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideColorRemoteRepository(
        api: TheColorApiService
    ): ColorRemoteRepository =
        ColorRemoteRepositoryImpl(api)
}