package com.ordolabs.data.di

import com.ordolabs.data.repository.ColorRemoteRepository
import com.ordolabs.data.repository.ColorValidatorRepository
import com.ordolabs.data.repository.ColorsHistoryRepository
import com.ordolabs.domain.repository.IColorRemoteRepository
import com.ordolabs.domain.repository.IColorValidatorRepository
import com.ordolabs.domain.repository.IColorsHistoryRepository
import org.koin.dsl.module

val repositoryModule = module {

    single<IColorValidatorRepository> {
        ColorValidatorRepository()
    }

    single<IColorsHistoryRepository> {
        ColorsHistoryRepository(colorsHistoryDao = get() )
    }

    single<IColorRemoteRepository> { ColorRemoteRepository(api = get()) }
}