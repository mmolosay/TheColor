package com.ordolabs.thecolor

import android.app.Application
import com.ordolabs.data.di.localDataSourceModule
import com.ordolabs.data.di.networkModule
import com.ordolabs.data.di.repositoryModule
import com.ordolabs.domain.di.useCaseModule
import com.ordolabs.thecolor.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

internal class TheColorApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@TheColorApplication)
            modules(
                viewModelModule,
                repositoryModule,
                localDataSourceModule,
                networkModule,
                useCaseModule
            )
        }
    }
}