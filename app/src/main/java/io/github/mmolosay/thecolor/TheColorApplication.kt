package io.github.mmolosay.thecolor

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import timber.log.Timber

@HiltAndroidApp
class TheColorApplication : Application(), ApplicationCoroutineScopeProvider {

    override val applicationScope: CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default + CoroutineName("Application CoroutineScope"))

    override fun onCreate() {
        super.onCreate()
        initApplicationScope()
        initTimber()
    }

    private fun initApplicationScope() {
        val observer = object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                applicationScope.cancel("Application process was destroyed.")
            }
        }
        ProcessLifecycleOwner.get().lifecycle.addObserver(observer)
    }

    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}