package io.github.mmolosay.thecolor.domain.usecase

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.model.ColorScheme
import io.github.mmolosay.thecolor.domain.repository.ColorRepository
import javax.inject.Inject

class GetColorSchemeUseCase @Inject constructor(
    private val colorRepository: ColorRepository,
) {

    suspend operator fun invoke(request: Request): ColorScheme =
        colorRepository.getColorScheme(request)

    data class Request(
        val seed: Color,
        val mode: ColorScheme.Mode,
        val swatchCount: Int,
    )
}