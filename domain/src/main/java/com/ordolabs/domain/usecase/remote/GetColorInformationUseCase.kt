package com.ordolabs.domain.usecase.remote

import com.ordolabs.domain.model.ColorInformation
import com.ordolabs.domain.repository.IColorRemoteRepository
import com.ordolabs.domain.usecase.BaseUseCase

typealias GetColorInformationBaseUseCase = BaseUseCase<String, ColorInformation>

class GetColorInformationUseCase(
    private val colorRemoteRepository: IColorRemoteRepository
) : GetColorInformationBaseUseCase {

    override suspend fun invoke(param: String) =
        colorRemoteRepository.fetchColorInformation(param)
}