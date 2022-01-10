package com.ordolabs.domain.usecase.remote

import com.ordolabs.domain.model.ColorScheme
import com.ordolabs.domain.repository.IColorRemoteRepository
import com.ordolabs.domain.usecase.BaseUseCase
import kotlinx.coroutines.flow.Flow

typealias GetColorSchemeBaseUseCase = BaseUseCase<String, ColorScheme>

class GetColorSchemeUseCase(
    private val colorRemoteRepository: IColorRemoteRepository
) : GetColorSchemeBaseUseCase {

    override suspend fun invoke(param: String): Flow<ColorScheme> =
        colorRemoteRepository.fetchColorScheme(param)
}