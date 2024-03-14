package io.github.mmolosay.thecolor.domain.usecase

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.model.ColorDetails
import io.github.mmolosay.thecolor.domain.repository.ColorRepository
import javax.inject.Inject

class GetColorDetailsUseCase @Inject constructor(
    private val colorRepository: ColorRepository,
) {

    suspend operator fun invoke(color: Color): ColorDetails =
        colorRepository.getColorDetails(color)

}