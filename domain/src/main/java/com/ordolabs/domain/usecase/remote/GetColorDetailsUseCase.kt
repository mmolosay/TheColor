package com.ordolabs.domain.usecase.remote

import com.ordolabs.domain.model.ColorDetails
import com.ordolabs.domain.repository.IColorRemoteRepository
import com.ordolabs.domain.usecase.BaseUseCase

typealias GetColorDetailsBaseUseCase = BaseUseCase<String, ColorDetails>

class GetColorDetailsUseCase(
    private val colorRemoteRepository: IColorRemoteRepository
) : GetColorDetailsBaseUseCase {

    override suspend fun invoke(param: String) =
        colorRemoteRepository.fetchColorDetails(param)
}