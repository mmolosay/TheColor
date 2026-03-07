package io.github.mmolosay.thecolor.domain.buildfeatures

import javax.inject.Inject

class IsDevOptionsEnabledUseCase @Inject constructor(
    private val buildInfoRepository: BuildInfoRepository,
) {

    operator fun invoke(): Boolean {
        val buildTypesWhereEnabled = listOf(BuildType.Debug, BuildType.QA)
        return (buildInfoRepository.getAppBuildType() in buildTypesWhereEnabled)
    }
}