package com.ordolabs.core.di

import com.ordolabs.domain.usecase.local.ValidateColorHexBaseUseCase
import com.ordolabs.domain.usecase.local.ValidateColorRgbBaseUseCase
import com.ordolabs.domain.usecase.remote.GetColorDetailsBaseUseCase
import com.ordolabs.domain.usecase.remote.GetColorSchemeBaseUseCase

interface CoreComponentProviders {

    // region Color input

    val validateColorHexUseCase: ValidateColorHexBaseUseCase
    val validateColorRgbUseCase: ValidateColorRgbBaseUseCase

    // endregion

    // region Color data

    val getColorDetailsUseCase: GetColorDetailsBaseUseCase
    val getColorSchemeUseCase: GetColorSchemeBaseUseCase

    // endregion
}