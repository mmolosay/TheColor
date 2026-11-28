package io.github.mmolosay.thecolor.presentation.devoptions.viewmodel

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.buildfeatures.BuildInfoRepository
import io.github.mmolosay.thecolor.domain.dev.options.DefaultDevOptions
import io.github.mmolosay.thecolor.utils.composition.Change
import io.github.mmolosay.thecolor.domain.dev.options.DevOptions.HttpLogging as DomainHttpLogging
import io.github.mmolosay.thecolor.domain.dev.options.DevOptions.PredictableRandomColors as DomainPredictableRandomColors
import io.github.mmolosay.thecolor.domain.dev.options.DevOptions.StrictMode as DomainStrictMode

class DevOptionsCore @AssistedInject constructor(
    private val defaultDevOptions: DefaultDevOptions,
    private val buildInfoRepository: BuildInfoRepository,
) {

    private val buildInfo by lazy {
        DevOptionsViewData.BuildInfo(
            appBuildType = buildInfoRepository.getAppBuildType(),
            appVersionName = buildInfoRepository.getAppBuildVersionName(),
            appVersionCode = buildInfoRepository.getAppBuildVersionCode(),
            gitHeadCommitHash = buildInfoRepository.getGitHeadCommitShortHash().toString(),
        )
    }

    // region Getters

    fun DevOptionsState.toViewData(): DevOptionsViewData? {
        return DevOptionsViewData(
            predictableRandomColors = this.predictableRandomColors ?: return null,
            predictableRandomColorsByDefault = defaultDevOptions.predictableRandomColors,
            isStrictModeEnabled = this.strictMode?.enabled ?: return null,
            isStrictModeEnabledByDefault = defaultDevOptions.strictMode.enabled,
            isHttpLoggingEnabled = this.httpLogging?.enabled ?: return null,
            isHttpLoggingEnabledByDefault = defaultDevOptions.httpLogging.enabled,
            buildInfo = buildInfo,
        )
    }

    // endregion

    // region Transitions

    fun DevOptionsState.applyPredictableRandomColors(
        value: DomainPredictableRandomColors,
    ): Change<DevOptionsState, Nothing> {
        val state = this.copy(predictableRandomColors = value)
        return Change(state)
    }

    fun DevOptionsState.applyStrictMode(
        value: DomainStrictMode,
    ): Change<DevOptionsState, Nothing> {
        val state = this.copy(strictMode = value)
        return Change(state)
    }

    fun DevOptionsState.applyHttpLogging(
        value: DomainHttpLogging,
    ): Change<DevOptionsState, Nothing> {
        val state = this.copy(httpLogging = value)
        return Change(state)
    }

    // endregion

    @AssistedFactory
    fun interface Factory {
        fun create(): DevOptionsCore
    }
}