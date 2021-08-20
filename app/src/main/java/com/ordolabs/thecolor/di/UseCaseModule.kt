package com.ordolabs.thecolor.di

import com.ordolabs.domain.repository.IColorInfoRepository
import com.ordolabs.domain.repository.IColorValidatorRepository
import com.ordolabs.domain.usecase.GetColorInfoBaseUseCase
import com.ordolabs.domain.usecase.GetColorInfoUseCase
import com.ordolabs.domain.usecase.local.ValidateColorHexBaseUseCase
import com.ordolabs.domain.usecase.local.ValidateColorHexUseCase
import com.ordolabs.domain.usecase.local.ValidateColorRgbBaseUseCase
import com.ordolabs.domain.usecase.local.ValidateColorRgbUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

val useCaseModule = module {

    single(named(VALIDATE_COLOR_HEX_USE_CASE)) { provideValidateColorHexUseCase(get()) }
    single(named(VALIDATE_COLOR_RGB_USE_CASE)) { provideValidateColorRgbUseCase(get()) }

    single(named(GET_COLOR_INFO_USE_CASE)) { provideGetColorInfoUseCase(get()) }
}

const val VALIDATE_COLOR_HEX_USE_CASE = "validate_color_hex_use_case"
const val VALIDATE_COLOR_RGB_USE_CASE = "validate_color_rgb_use_case"

const val GET_COLOR_INFO_USE_CASE = "get_color_info"

fun provideValidateColorHexUseCase(repository: IColorValidatorRepository): ValidateColorHexBaseUseCase {
    return ValidateColorHexUseCase(repository)
}

fun provideValidateColorRgbUseCase(repository: IColorValidatorRepository): ValidateColorRgbBaseUseCase {
    return ValidateColorRgbUseCase(repository)
}

fun provideGetColorInfoUseCase(getColorInfoRepository: IColorInfoRepository): GetColorInfoBaseUseCase {
    return GetColorInfoUseCase(getColorInfoRepository)
}