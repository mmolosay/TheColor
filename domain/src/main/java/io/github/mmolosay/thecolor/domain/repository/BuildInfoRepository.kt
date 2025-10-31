package io.github.mmolosay.thecolor.domain.repository

import io.github.mmolosay.thecolor.domain.model.BuildType

interface BuildInfoRepository {
    fun getAppBuildType(): BuildType
    fun getAppBuildVersionCode(): Long
    fun getAppBuildVersionName(): String?
}