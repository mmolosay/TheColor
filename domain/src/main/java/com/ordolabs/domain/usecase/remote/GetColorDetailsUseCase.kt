package com.ordolabs.domain.usecase.remote

import com.ordolabs.domain.model.ColorDetails
import com.ordolabs.domain.repository.IColorRemoteRepository
import com.ordolabs.domain.usecase.BaseUseCase
import kotlinx.coroutines.flow.Flow

typealias GetColorDetailsBaseUseCase = BaseUseCase<String, ColorDetails>

class GetColorDetailsUseCase(
    private val colorRemoteRepository: IColorRemoteRepository
) : GetColorDetailsBaseUseCase {

    override suspend fun invoke(param: String): Flow<ColorDetails> =
        colorRemoteRepository.fetchColorDetails(param)
}