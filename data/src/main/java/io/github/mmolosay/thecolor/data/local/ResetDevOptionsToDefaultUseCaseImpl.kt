package io.github.mmolosay.thecolor.data.local

import io.github.mmolosay.thecolor.domain.dev.options.DevOptionsRepository
import io.github.mmolosay.thecolor.domain.dev.options.ResetDevOptionsToDefaultUseCase
import javax.inject.Inject

class ResetDevOptionsToDefaultUseCaseImpl @Inject constructor(
    private val devOptionsRepository: DevOptionsRepository,
) : ResetDevOptionsToDefaultUseCase {

    override suspend fun invoke() {
        // updating values sequentially vs concurrently take about the same time ~68ms
        devOptionsRepository.setPredictableRandomColors(null)
        devOptionsRepository.setStrictMode(null)
        devOptionsRepository.setHttpLogging(null)
    }
}