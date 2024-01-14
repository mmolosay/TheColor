package com.ordolabs.domain.usecase

import com.ordolabs.domain.model.ColorDetails
import com.ordolabs.domain.repository.ColorRepository
import javax.inject.Inject

class GetColorDetailsUseCase @Inject constructor(
    private val colorRepository: ColorRepository,
) {

    suspend operator fun invoke(colorHex: String): ColorDetails =
        colorRepository.getColorDetails(colorHex)
}