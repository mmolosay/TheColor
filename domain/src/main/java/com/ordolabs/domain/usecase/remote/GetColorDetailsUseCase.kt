package com.ordolabs.domain.usecase.remote

import com.ordolabs.domain.model.ColorDetails
import com.ordolabs.domain.repository.ColorRemoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetColorDetailsUseCase @Inject constructor(
    private val colorRemoteRepository: ColorRemoteRepository
) {

    suspend fun invoke(param: String): Flow<ColorDetails> =
        colorRemoteRepository.fetchColorDetails(param)
}