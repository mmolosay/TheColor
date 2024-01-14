package com.ordolabs.domain.usecase.remote

import com.ordolabs.domain.model.ColorScheme
import com.ordolabs.domain.model.ColorSchemeRequest
import com.ordolabs.domain.repository.ColorRemoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetColorSchemeUseCase @Inject constructor(
    private val colorRemoteRepository: ColorRemoteRepository,
) {

    suspend fun invoke(param: ColorSchemeRequest): Flow<ColorScheme> =
        colorRemoteRepository.fetchColorScheme(param)
}