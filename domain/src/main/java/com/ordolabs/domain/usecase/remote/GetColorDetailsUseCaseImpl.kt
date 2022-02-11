package com.ordolabs.domain.usecase.remote

import com.ordolabs.domain.model.ColorDetails
import com.ordolabs.domain.repository.ColorRemoteRepository
import com.ordolabs.domain.usecase.BaseUseCase
import kotlinx.coroutines.flow.Flow

interface GetColorDetailsUseCase : BaseUseCase<String, ColorDetails>

class GetColorDetailsUseCaseImpl(
    private val colorRemoteRepository: ColorRemoteRepository
) : GetColorDetailsUseCase {

    override suspend fun invoke(param: String): Flow<ColorDetails> =
        colorRemoteRepository.fetchColorDetails(param)
}