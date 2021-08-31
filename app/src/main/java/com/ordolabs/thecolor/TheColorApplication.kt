package com.ordolabs.thecolor

import android.app.Application
import com.ordolabs.di.localDataSourceModule
import com.ordolabs.di.networkModule
import com.ordolabs.di.repositoryModule
import com.ordolabs.di.useCaseModule
import com.ordolabs.thecolor.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

internal class TheColorApplication : Application() {

    override fun onCreate() {
        super.onCreate()

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