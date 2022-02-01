package com.ordolabs.thecolor

import android.app.Application
import com.ordolabs.di.AppComponent
import com.ordolabs.di.DaggerAppComponent
import com.ordolabs.di.localDataSourceModule
import com.ordolabs.di.networkModule
import com.ordolabs.di.repositoryModule
import com.ordolabs.di.useCaseModule
import com.ordolabs.thecolor.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

internal class TheColorApplication : Application() {

    val appComponent: AppComponent
        get() = requireNotNull(_appComponent) { "AppComponent is not initialized yet" }

    private var _appComponent: AppComponent? = null

    override fun onCreate() {
        super.onCreate()

        _appComponent = DaggerAppComponent.create()

        startKoin {
            androidContext(this@TheColorApplication)
            modules(
                appModule,
                repositoryModule,
                localDataSourceModule,
                networkModule,
                useCaseModule
            )
        }
    }
}