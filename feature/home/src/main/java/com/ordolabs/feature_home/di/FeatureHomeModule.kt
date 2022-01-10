package com.ordolabs.feature_home.di

import androidx.lifecycle.SavedStateHandle
import com.ordolabs.di.GET_COLOR_DETAILS_USE_CASE
import com.ordolabs.di.VALIDATE_COLOR_HEX_USE_CASE
import com.ordolabs.di.VALIDATE_COLOR_RGB_USE_CASE
import com.ordolabs.feature_home.viewmodel.ColorInputViewModel
import com.ordolabs.feature_home.viewmodel.HomeViewModel
import com.ordolabs.feature_home.viewmodel.colordata.ColorDataViewModel
import com.ordolabs.feature_home.viewmodel.colordata.details.ColorDetailsObtainViewModel
import com.ordolabs.feature_home.viewmodel.colordata.details.ColorDetailsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val featureHomeModule = module {

    viewModel {
        HomeViewModel(
            state = SavedStateHandle()
        )
    }

    viewModel {
        ColorInputViewModel(
            validateColorHexUseCase = get(named(VALIDATE_COLOR_HEX_USE_CASE)),
            validateColorRgbUseCase = get(named(VALIDATE_COLOR_RGB_USE_CASE))
        )
    }

    // region Color Data

    viewModel {
        ColorDataViewModel()
    }

    viewModel {
        ColorDetailsObtainViewModel(
            getColorDetailsUseCase = get(named(GET_COLOR_DETAILS_USE_CASE))
        )
    }

    viewModel {
        ColorDetailsViewModel()
    }

    // endregion
}