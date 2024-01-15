package io.github.mmolosay.thecolor.domain.usecase

import io.github.mmolosay.thecolor.domain.model.ColorScheme
import io.github.mmolosay.thecolor.domain.model.ColorSchemeRequest
import io.github.mmolosay.thecolor.domain.repository.ColorRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetColorSchemeUseCase @Inject constructor(
    private val colorRepository: ColorRepository,
) {

    suspend operator fun invoke(request: ColorSchemeRequest): ColorScheme =
        colorRepository.getColorScheme(request)
}