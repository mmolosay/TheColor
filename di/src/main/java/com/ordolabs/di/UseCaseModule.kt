package com.ordolabs.di

import com.ordolabs.domain.repository.IColorRemoteRepository
import com.ordolabs.domain.repository.IColorValidatorRepository
import com.ordolabs.domain.usecase.local.ValidateColorHexBaseUseCase
import com.ordolabs.domain.usecase.local.ValidateColorHexUseCase
import com.ordolabs.domain.usecase.local.ValidateColorRgbBaseUseCase
import com.ordolabs.domain.usecase.local.ValidateColorRgbUseCase
import com.ordolabs.domain.usecase.remote.GetColorDetailsBaseUseCase
import com.ordolabs.domain.usecase.remote.GetColorDetailsUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

val useCaseModule = module {

    single(named(VALIDATE_COLOR_HEX_USE_CASE)) { provideValidateColorHexUseCase(get()) }
    single(named(VALIDATE_COLOR_RGB_USE_CASE)) { provideValidateColorRgbUseCase(get()) }

    single(named(GET_COLOR_DETAILS_USE_CASE)) { provideGetColorDetailsUseCase(get()) }
}

const val VALIDATE_COLOR_HEX_USE_CASE = "validate_color_hex_use_case"
const val VALIDATE_COLOR_RGB_USE_CASE = "validate_color_rgb_use_case"

const val GET_COLOR_DETAILS_USE_CASE = "get_color_details_use_case"

fun provideValidateColorHexUseCase(repository: IColorValidatorRepository): ValidateColorHexBaseUseCase {
    return ValidateColorHexUseCase(repository)
}

fun provideValidateColorRgbUseCase(repository: IColorValidatorRepository): ValidateColorRgbBaseUseCase {
    return ValidateColorRgbUseCase(repository)
}

fun provideGetColorDetailsUseCase(repository: IColorRemoteRepository): GetColorDetailsBaseUseCase {
    return GetColorDetailsUseCase(repository)
}