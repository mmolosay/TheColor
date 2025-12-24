package io.github.mmolosay.thecolor.domain.repository

import io.github.mmolosay.thecolor.domain.model.BuildType
import io.github.mmolosay.thecolor.domain.model.DevOptions
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stores default values of options of "Developer Options" feature.
 * They are used when there's no user-overridden value defined, or when a reset is done.
 */
@Singleton // in theory, there shouldn't be a case where multiple instances of this class are required
class DefaultDevOptions @Inject constructor(
    val buildInfoRepository: BuildInfoRepository,
) {

    val predictableRandomColors: DevOptions.PredictableRandomColors =
        DevOptions.PredictableRandomColors.Random

    val strictMode: DevOptions.StrictMode by lazy {
        /*
         * Don't enabled Strict mode in the "Debug" build type by default because
         * pauses of the execution on the debugger's break points trigger Strict mode's penalties,
         * which may interfere with debugging and development.
         */
        val buildTypesWhereEnabled = listOf(BuildType.QA)
        val enabled = (buildInfoRepository.getAppBuildType() in buildTypesWhereEnabled)
        DevOptions.StrictMode(enabled)
    }
}