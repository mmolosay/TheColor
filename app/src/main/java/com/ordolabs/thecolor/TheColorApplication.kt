package com.ordolabs.thecolor

import android.app.Application
import com.ordolabs.thecolor.di.localDataSourceModule
import com.ordolabs.thecolor.di.networkModule
import com.ordolabs.thecolor.di.remoteDataSourceModule
import com.ordolabs.thecolor.di.useCaseModule
import com.ordolabs.thecolor.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin


internal class TheColorApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@TheColorApplication)
            modules(
                networkModule,
                viewModelModule,
                localDataSourceModule,
                remoteDataSourceModule,
                useCaseModule
            )
        }
    }
}