package com.ordolabs.thecolor.di

import com.ordolabs.domain.repository.IColorInfoRepository
import com.ordolabs.domain.usecase.GetColorInfoBaseUseCase
import com.ordolabs.domain.usecase.GetColorInfoUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

val useCaseModule = module {

    single(named(GET_COLOR_INFO_USE_CASE)) { provideGetColorInfoUseCase(get()) }
}

const val GET_COLOR_INFO_USE_CASE = "get_color_info"

fun provideGetColorInfoUseCase(getColorInfoRepository: IColorInfoRepository): GetColorInfoBaseUseCase {
    return GetColorInfoUseCase(getColorInfoRepository)
}