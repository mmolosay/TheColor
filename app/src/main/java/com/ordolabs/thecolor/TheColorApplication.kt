package com.ordolabs.thecolor

import android.app.Application
import android.content.Context
import com.ordolabs.core.di.core.DaggerCoreComponent
import com.ordolabs.data_bridge.DaggerDataComponentImpl
import com.ordolabs.thecolor.di.AppComponent
import com.ordolabs.thecolor.di.DaggerAppComponent

internal class TheColorApplication : Application() {

    val appComponent: AppComponent
        get() = requireNotNull(_appComponent) { "AppComponent is not initialized yet" }

    private var _appComponent: AppComponent? = null

    override fun onCreate() {
        super.onCreate()
        setDagger()
    }

    private fun setDagger() {
        val dataComponent = DaggerDataComponentImpl
            .builder()
            .build()
        val coreComponent = DaggerCoreComponent
            .builder()
            .dataComponent(dataComponent)
            .build()
        val appComponentDependencies = AppComponentDependenciesImpl()
        this._appComponent = DaggerAppComponent
            .builder()
            .coreComponent(coreComponent)
            .dependencies(appComponentDependencies)
            .build()
    }

    private inner class AppComponentDependenciesImpl : AppComponent.Dependencies {

        override val applicationContext: Context = this@TheColorApplication
    }
}