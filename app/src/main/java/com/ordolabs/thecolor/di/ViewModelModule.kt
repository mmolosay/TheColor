package com.ordolabs.thecolor.di

import com.ordolabs.thecolor.viewmodel.ColorInformationViewModel
import com.ordolabs.thecolor.viewmodel.ColorInputViewModel
import com.ordolabs.thecolor.viewmodel.HomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val viewModelModule = module {

    viewModel {
        HomeViewModel(
            // nothing is here
        )
    }

    viewModel {
        ColorInputViewModel(
            validateColorHexUseCase = get(named(VALIDATE_COLOR_HEX_USE_CASE)),
            validateColorRgbUseCase = get(named(VALIDATE_COLOR_RGB_USE_CASE))
        )
    }

    viewModel {
        ColorInformationViewModel(
            getColorInformationUseCase = get(named(GET_COLOR_INFORMATION_USE_CASE))
        )
    }

}