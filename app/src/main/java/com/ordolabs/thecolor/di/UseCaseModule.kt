package com.ordolabs.thecolor.di

import com.ordolabs.domain.repository.IColorRemoteRepository
import com.ordolabs.domain.repository.IColorValidatorRepository
import com.ordolabs.domain.usecase.local.ValidateColorHexBaseUseCase
import com.ordolabs.domain.usecase.local.ValidateColorHexUseCase
import com.ordolabs.domain.usecase.local.ValidateColorRgbBaseUseCase
import com.ordolabs.domain.usecase.local.ValidateColorRgbUseCase
import com.ordolabs.domain.usecase.remote.GetColorInformationBaseUseCase
import com.ordolabs.domain.usecase.remote.GetColorInformationUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

val useCaseModule = module {

    single(named(VALIDATE_COLOR_HEX_USE_CASE)) { provideValidateColorHexUseCase(get()) }
    single(named(VALIDATE_COLOR_RGB_USE_CASE)) { provideValidateColorRgbUseCase(get()) }

    single(named(GET_COLOR_INFORMATION_USE_CASE)) { provideGetColorInformationUseCase(get()) }
}

const val VALIDATE_COLOR_HEX_USE_CASE = "validate_color_hex_use_case"
const val VALIDATE_COLOR_RGB_USE_CASE = "validate_color_rgb_use_case"

const val GET_COLOR_INFORMATION_USE_CASE = "get_color_information_use_case"

fun provideValidateColorHexUseCase(repository: IColorValidatorRepository): ValidateColorHexBaseUseCase {
    return ValidateColorHexUseCase(repository)
}

fun provideValidateColorRgbUseCase(repository: IColorValidatorRepository): ValidateColorRgbBaseUseCase {
    return ValidateColorRgbUseCase(repository)
}

fun provideGetColorInformationUseCase(repository: IColorRemoteRepository): GetColorInformationBaseUseCase {
    return GetColorInformationUseCase(repository)
}