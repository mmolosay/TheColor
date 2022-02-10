package com.ordolabs.domain.di

import com.ordolabs.domain.usecase.local.ValidateColorHexBaseUseCase
import com.ordolabs.domain.usecase.local.ValidateColorRgbBaseUseCase
import com.ordolabs.domain.usecase.remote.GetColorDetailsBaseUseCase
import com.ordolabs.domain.usecase.remote.GetColorSchemeBaseUseCase

interface DomainProvisions {

    val validateColorUseCase: ValidateColorHexBaseUseCase
    val validateColorRgbUseCase: ValidateColorRgbBaseUseCase

    val getColorDetailsUseCase: GetColorDetailsBaseUseCase
    val getColorSchemeUseCase: GetColorSchemeBaseUseCase
}