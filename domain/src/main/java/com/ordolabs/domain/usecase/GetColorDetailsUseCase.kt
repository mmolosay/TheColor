package com.ordolabs.domain.usecase

import com.ordolabs.domain.model.ColorDetails
import com.ordolabs.domain.repository.ColorRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetColorDetailsUseCase @Inject constructor(
    private val colorRepository: ColorRepository,
) {

    suspend operator fun invoke(param: String): Flow<ColorDetails> =
        colorRepository.getColorDetails(param)
}