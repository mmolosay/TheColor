package com.ordolabs.domain.di

import com.ordolabs.domain.usecase.local.ValidateColorHexUseCase
import com.ordolabs.domain.usecase.local.ValidateColorRgbUseCase
import com.ordolabs.domain.usecase.remote.GetColorDetailsUseCase
import com.ordolabs.domain.usecase.remote.GetColorSchemeUseCase

interface DomainProvisions {

    val validateColorUseCase: ValidateColorHexUseCase
    val validateColorRgbUseCase: ValidateColorRgbUseCase

    val getColorDetailsUseCase: GetColorDetailsUseCase
    val getColorSchemeUseCase: GetColorSchemeUseCase
}