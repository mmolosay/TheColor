package com.ordolabs.thecolor.di

import com.ordolabs.data.remote.repository.ColorRemoteRepository
import com.ordolabs.domain.repository.IColorRemoteRepository
import org.koin.dsl.module

val remoteDataSourceModule = module {

    single<IColorRemoteRepository> { ColorRemoteRepository(api = get()) }
}