package io.github.mmolosay.thecolor.domain.usecase

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.model.ColorScheme
import io.github.mmolosay.thecolor.domain.model.OldColorScheme
import io.github.mmolosay.thecolor.domain.repository.ColorRepository
import javax.inject.Inject
import io.github.mmolosay.thecolor.domain.model.ColorSchemeRequest as OldColorSchemeRequest

class GetColorSchemeUseCase @Inject constructor(
    private val colorRepository: ColorRepository,
) {

    // TODO: remove once old View UI is gone
    suspend operator fun invoke(request: OldColorSchemeRequest): OldColorScheme =
        colorRepository.getColorScheme(request)

    suspend operator fun invoke(request: Request): ColorScheme =
        colorRepository.getColorScheme(request)

    data class Request(
        val seed: Color,
        val mode: ColorScheme.Mode,
        val swatchCount: Int,
    )
}