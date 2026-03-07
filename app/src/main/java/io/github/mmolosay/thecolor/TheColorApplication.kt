package io.github.mmolosay.thecolor

import android.app.Application
import android.os.Looper
import android.os.StrictMode
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.HiltAndroidApp
import io.github.mmolosay.thecolor.domain.buildfeatures.AreLogsEnabledUseCase
import io.github.mmolosay.thecolor.domain.dev.options.DefaultDevOptions
import io.github.mmolosay.thecolor.domain.dev.options.DevOptionsRepository
import io.github.mmolosay.thecolor.domain.dev.options.filterOutBeingInitialized
import io.github.mmolosay.thecolor.domain.dev.options.valueOrElse
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class TheColorApplication : Application(), ApplicationCoroutineScopeProvider {

    override val applicationScope: CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default + CoroutineName("Application CoroutineScope"))

    @Inject
    lateinit var areLogsEnabled: AreLogsEnabledUseCase

    @Inject
    lateinit var devOptionsRepository: DevOptionsRepository

    @Inject
    lateinit var defaultDevOptions: DefaultDevOptions

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
        applicationScope.launch {
            val enabled = devOptionsRepository.flowOfStrictMode
                .filterOutBeingInitialized() // await for the first read value
                .first().valueOrElse { defaultDevOptions.strictMode }
                .enabled
            if (enabled) {
                withContext(Dispatchers.Main) { // StrictMode must be applied to the main thread
                    check(Looper.getMainLooper().isCurrentThread)
                    val threadPolicy = StrictMode.ThreadPolicy.Builder()
                        .detectAll()
                        .penaltyLog()
                        .penaltyFlashScreen()
                        .build()
                    StrictMode.setThreadPolicy(threadPolicy)
                }
            }
        }
    }
}