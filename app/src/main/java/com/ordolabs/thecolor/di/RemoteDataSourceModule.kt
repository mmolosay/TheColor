package com.ordolabs.thecolor.di

import com.ordolabs.data.remote.repository.ColorInfoRepository
import com.ordolabs.domain.repository.IColorInfoRepository
import org.koin.dsl.module

val remoteDataSourceModule = module {

    single<IColorInfoRepository> { ColorInfoRepository(api = get()) }
}