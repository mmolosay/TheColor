package com.ordolabs.feature_home.di

import com.ordolabs.domain.di.GET_COLOR_INFORMATION_USE_CASE
import com.ordolabs.domain.di.VALIDATE_COLOR_HEX_USE_CASE
import com.ordolabs.domain.di.VALIDATE_COLOR_RGB_USE_CASE
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val featureHomeModule = module {

    viewModel {
        com.ordolabs.feature_home.viewmodel.ColorInputViewModel(
            validateColorHexUseCase = get(named(VALIDATE_COLOR_HEX_USE_CASE)),
            validateColorRgbUseCase = get(named(VALIDATE_COLOR_RGB_USE_CASE))
        )
    }

    viewModel {
        com.ordolabs.feature_home.viewmodel.ColorInformationViewModel(
            getColorInformationUseCase = get(named(GET_COLOR_INFORMATION_USE_CASE))
        )
    }
}