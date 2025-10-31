package io.github.mmolosay.thecolor.data.local

import android.content.Context
import androidx.core.content.pm.PackageInfoCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.mmolosay.thecolor.domain.model.BuildType
import io.github.mmolosay.thecolor.domain.repository.BuildInfoRepository
import io.github.mmolosay.thecolor.domain.usecase.GetAppBuildTypeUseCase
import java.lang.ref.WeakReference
import javax.inject.Inject

class BuildInfoRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
    private val getAppBuildType: GetAppBuildTypeUseCase,
) : BuildInfoRepository {

    private val context = WeakReference(context)

    override fun getAppBuildType(): BuildType =
        getAppBuildType.invoke()

    override fun getAppBuildVersionCode(): Long {
        val context = context.get() ?: nullContextError()
        val packageInfo = context.packageManager.getPackageInfo(/*packageName*/ context.packageName, /*flags*/0)
        return PackageInfoCompat.getLongVersionCode(packageInfo)
    }

    override fun getAppBuildVersionName(): String? {
        val context = context.get() ?: nullContextError()
        val packageInfo = context.packageManager.getPackageInfo(/*packageName*/ context.packageName, /*flags*/0)
        return packageInfo.versionName
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun nullContextError(): Nothing =
        error("context is null")
}