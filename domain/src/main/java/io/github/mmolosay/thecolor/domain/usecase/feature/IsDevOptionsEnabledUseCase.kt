package io.github.mmolosay.thecolor.domain.usecase.feature

import io.github.mmolosay.thecolor.domain.model.BuildType
import io.github.mmolosay.thecolor.domain.usecase.GetAppBuildTypeUseCase
import javax.inject.Inject

class IsDevOptionsEnabledUseCase @Inject constructor(
    val getBuildType: GetAppBuildTypeUseCase,
) {

    operator fun invoke(): Boolean {
        val buildTypesWhereEnabled = listOf(BuildType.Debug)
        return (getBuildType() in buildTypesWhereEnabled)
    }
}