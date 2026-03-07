package io.github.mmolosay.thecolor.presentation.devoptions

import io.github.mmolosay.thecolor.domain.buildfeatures.BuildType as DomainBuildType
import io.github.mmolosay.thecolor.domain.dev.options.DevOptions.PredictableRandomColors as DomainPredictableRandomColors

/**
 * Platform-agnostic data provided by ViewModel to 'Developer Options' View.
 */
data class DevOptionsData(
    val resetValuesToDefault: () -> Unit,

    val predictableRandomColors: DomainPredictableRandomColors, // it's OK to use some domain models (like enums) in presentation layer
    val predictableRandomColorsByDefault: DomainPredictableRandomColors,
    val changePredictableRandomColors: (DomainPredictableRandomColors) -> Unit,

    val isStrictModeEnabled: Boolean,
    val isStrictModeEnabledByDefault: Boolean,
    val changeStrictModeEnablement: (Boolean) -> Unit,

    val isHttpLoggingEnabled: Boolean,
    val isHttpLoggingEnabledByDefault: Boolean,
    val changeHttpLoggingEnablement: (Boolean) -> Unit,

    val buildInfo: BuildInfo,
) {

    data class BuildInfo(
        val appBuildType: DomainBuildType,
        val appVersionName: String?,
        val appVersionCode: Long,
    )
}