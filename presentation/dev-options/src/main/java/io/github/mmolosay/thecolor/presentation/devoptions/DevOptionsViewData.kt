package io.github.mmolosay.thecolor.presentation.devoptions

import io.github.mmolosay.thecolor.domain.buildfeatures.BuildType as DomainBuildType
import io.github.mmolosay.thecolor.domain.dev.options.DevOptions.PredictableRandomColors as DomainPredictableRandomColors

data class DevOptionsViewData(
    val predictableRandomColors: DomainPredictableRandomColors,
    val predictableRandomColorsByDefault: DomainPredictableRandomColors,
    val isStrictModeEnabled: Boolean,
    val isStrictModeEnabledByDefault: Boolean,
    val isHttpLoggingEnabled: Boolean,
    val isHttpLoggingEnabledByDefault: Boolean,
    val buildInfo: BuildInfo,
) {

    data class BuildInfo(
        val appBuildType: DomainBuildType,
        val appVersionName: String?,
        val appVersionCode: Long,
        val gitHeadCommitHash: String,
    )
}