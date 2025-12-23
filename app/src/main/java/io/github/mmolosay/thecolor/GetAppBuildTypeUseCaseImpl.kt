package io.github.mmolosay.thecolor

import io.github.mmolosay.thecolor.domain.model.BuildType
import io.github.mmolosay.thecolor.domain.usecase.GetAppBuildTypeUseCase
import javax.inject.Inject

/**
 * An implementation of [GetAppBuildTypeUseCase] that returns the build type of the application
 * (the `:app` Gradle module to be precise).
 */
class GetAppBuildTypeUseCaseImpl @Inject constructor() : GetAppBuildTypeUseCase {

    override fun invoke(): BuildType {
        val buildTypeName = BuildConfig.BUILD_TYPE
        @Suppress("KotlinConstantConditions") // BuildConfig.BUILD_TYPE is a generated source value which IDE resolves as a const string
        return when (buildTypeName) {
            "release" -> BuildType.Release
            "debug" -> BuildType.Debug
            "qa" -> BuildType.QA
            else -> error("Unsupported build type: $buildTypeName")
        }
    }
}