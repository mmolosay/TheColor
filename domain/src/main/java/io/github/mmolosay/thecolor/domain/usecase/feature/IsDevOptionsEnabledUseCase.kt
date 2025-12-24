package io.github.mmolosay.thecolor.domain.usecase.feature

import io.github.mmolosay.thecolor.domain.model.BuildType
import io.github.mmolosay.thecolor.domain.repository.BuildInfoRepository
import javax.inject.Inject

class IsDevOptionsEnabledUseCase @Inject constructor(
    private val buildInfoRepository: BuildInfoRepository,
) {

    operator fun invoke(): Boolean {
        val buildTypesWhereEnabled = listOf(BuildType.Debug, BuildType.QA)
        return (buildInfoRepository.getAppBuildType() in buildTypesWhereEnabled)
    }
}