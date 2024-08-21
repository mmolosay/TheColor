package io.github.mmolosay.thecolor.domain.usecase

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.repository.ColorRepository
import javax.inject.Inject

/**
 * Retrieves [Color] to be used on application launch.
 */
class GetInitialColorUseCase @Inject constructor(
    private val colorRepository: ColorRepository,
) {

    // condition may be added in future: if (userPreferences.showLastSearchedColorOnAppStart) ..
    suspend operator fun invoke(): Color? =
        colorRepository.lastSearchedColor()
}