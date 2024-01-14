package com.ordolabs.domain.usecase.remote

import com.ordolabs.domain.model.ColorScheme
import com.ordolabs.domain.model.ColorSchemeRequest
import com.ordolabs.domain.repository.ColorRemoteRepository
import com.ordolabs.domain.usecase.BaseUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface GetColorSchemeUseCase : BaseUseCase<ColorSchemeRequest, ColorScheme>

class GetColorSchemeUseCaseImpl @Inject constructor(
    private val colorRemoteRepository: ColorRemoteRepository
) : GetColorSchemeUseCase {

    override suspend fun invoke(param: ColorSchemeRequest): Flow<ColorScheme> =
        colorRemoteRepository.fetchColorScheme(param)
}