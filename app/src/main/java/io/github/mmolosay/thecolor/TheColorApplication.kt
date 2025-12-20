package io.github.mmolosay.thecolor

import android.app.Application
import android.os.StrictMode
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.HiltAndroidApp
import io.github.mmolosay.thecolor.domain.usecase.feature.AreLogsEnabledUseCase
import io.github.mmolosay.thecolor.domain.usecase.feature.IsStrictModeEnabledUseCase
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class TheColorApplication : Application(), ApplicationCoroutineScopeProvider {

    override val applicationScope: CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default + CoroutineName("Application CoroutineScope"))

    @Inject
    lateinit var areLogsEnabled: AreLogsEnabledUseCase

    @Inject
    lateinit var isStrictModeEnabled: IsStrictModeEnabledUseCase

    override fun onCreate() {
        super.onCreate()
        initApplicationScope()
        maybeInitTimber()
        maybeInitStrictMode()
    }

    private fun initApplicationScope() {
        val observer = object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                applicationScope.cancel("Application process was destroyed.")
            }
        }
        ProcessLifecycleOwner.get().lifecycle.addObserver(observer)
    }

    private fun maybeInitTimber() {
        if (areLogsEnabled()) {
            Timber.plant(TheColorTimberTree())
        }
    }

    private fun maybeInitStrictMode() {
        if (isStrictModeEnabled()) {
            val threadPolicy = StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .penaltyDeath()
                .build()
            StrictMode.setThreadPolicy(threadPolicy)
        }
    }
}