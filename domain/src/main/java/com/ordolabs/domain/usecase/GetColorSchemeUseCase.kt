package com.ordolabs.domain.usecase

import com.ordolabs.domain.model.ColorScheme
import com.ordolabs.domain.model.ColorSchemeRequest
import com.ordolabs.domain.repository.ColorRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetColorSchemeUseCase @Inject constructor(
    private val colorRepository: ColorRepository,
) {

    suspend operator fun invoke(request: ColorSchemeRequest): ColorScheme =
        colorRepository.getColorScheme(request)
}