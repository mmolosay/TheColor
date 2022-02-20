package com.ordolabs.domain.di.module

import com.ordolabs.domain.repository.ColorRemoteRepository
import com.ordolabs.domain.repository.ColorValidatorRepository
import com.ordolabs.domain.usecase.local.ValidateColorHexUseCase
import com.ordolabs.domain.usecase.local.ValidateColorHexUseCaseImpl
import com.ordolabs.domain.usecase.local.ValidateColorRgbUseCase
import com.ordolabs.domain.usecase.local.ValidateColorRgbUseCaseImpl
import com.ordolabs.domain.usecase.remote.GetColorDetailsUseCase
import com.ordolabs.domain.usecase.remote.GetColorDetailsUseCaseImpl
import com.ordolabs.domain.usecase.remote.GetColorSchemeUseCase
import com.ordolabs.domain.usecase.remote.GetColorSchemeUseCaseImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DomainUseCaseModule {

    @Provides
    @Singleton
    fun provideValidateColorHexUseCase(
        repository: ColorValidatorRepository
    ): ValidateColorHexUseCase =
        ValidateColorHexUseCaseImpl(repository)

    @Provides
    @Singleton
    fun provideValidateColorRgbUseCase(
        repository: ColorValidatorRepository
    ): ValidateColorRgbUseCase =
        ValidateColorRgbUseCaseImpl(repository)

    @Provides
    @Singleton
    fun provideGetColorDetailsUseCase(
        repository: ColorRemoteRepository
    ): GetColorDetailsUseCase =
        GetColorDetailsUseCaseImpl(repository)

    @Provides
    @Singleton
    fun provideGetColorSchemeUseCase(
        repository: ColorRemoteRepository
    ): GetColorSchemeUseCase =
        GetColorSchemeUseCaseImpl(repository)
}