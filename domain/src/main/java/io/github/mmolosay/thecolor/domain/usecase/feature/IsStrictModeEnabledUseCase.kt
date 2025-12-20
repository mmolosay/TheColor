package io.github.mmolosay.thecolor.domain.usecase.feature

import io.github.mmolosay.thecolor.domain.model.BuildType
import io.github.mmolosay.thecolor.domain.usecase.GetAppBuildTypeUseCase
import javax.inject.Inject

class IsStrictModeEnabledUseCase @Inject constructor(
    val getBuildType: GetAppBuildTypeUseCase,
) {

    operator fun invoke(): Boolean {
        /*
         * Don't enabled Strict mode in the "Debug" build type because pauses of the execution
         * on the debugger's break points trigger Strict mode's penalties
         * (specifically "penaltyDeath", which crashes the app, making debugging impossible).
         */
        val buildTypesWhereEnabled = listOf(BuildType.QA)
        return (getBuildType() in buildTypesWhereEnabled)
    }
}