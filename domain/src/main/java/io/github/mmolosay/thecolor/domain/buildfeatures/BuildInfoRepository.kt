package io.github.mmolosay.thecolor.domain.buildfeatures

interface BuildInfoRepository {
    fun getAppBuildType(): BuildType
    fun getAppBuildVersionCode(): Long
    fun getAppBuildVersionName(): String?
    fun getGitHeadCommitShortHash(): String?
}

/**
 * Returns the current [BuildType] of the application.
 *
 * Note: DO NOT USE THIS COMPONENT DIRECTLY. Use [BuildInfoRepository.getAppBuildType] instead.
 *
 * This component exists to enable Dependency Inversion: this interface is a high-level abstraction
 * that's implemented in a low-level module.
 *
 * Ideally, it should have been placed in some `:data:contracts` module (doesn't exist) with the following relations:
 * `:data` depends on -> `:data:contracts`
 * `:app` depends on -> `:data:contracts`
 * `:data:contracts` depends on -> `:domain`
 *
 * @see BuildInfoRepository.getAppBuildType
 */
interface AppBuildTypeProvider {
    fun get(): BuildType
}