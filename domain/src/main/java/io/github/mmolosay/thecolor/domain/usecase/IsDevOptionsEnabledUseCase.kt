package io.github.mmolosay.thecolor.domain.usecase

import io.github.mmolosay.thecolor.domain.model.BuildType
import javax.inject.Inject

class IsDevOptionsEnabledUseCase @Inject constructor(
    val getBuildType: GetAppBuildTypeUseCase,
) {

    operator fun invoke(): Boolean {
        val buildTypesWhereEnabled = listOf(BuildType.Debug)
        return (getBuildType() in buildTypesWhereEnabled)
    }
}