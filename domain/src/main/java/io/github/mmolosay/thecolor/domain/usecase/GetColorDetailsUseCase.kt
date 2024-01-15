package io.github.mmolosay.thecolor.domain.usecase

import io.github.mmolosay.thecolor.domain.model.ColorDetails
import io.github.mmolosay.thecolor.domain.repository.ColorRepository
import javax.inject.Inject

class GetColorDetailsUseCase @Inject constructor(
    private val colorRepository: ColorRepository,
) {

    suspend operator fun invoke(colorHex: String): ColorDetails =
        colorRepository.getColorDetails(colorHex)
}