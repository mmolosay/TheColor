package com.ordolabs.thecolor

import android.app.Application
import android.content.Context
import com.ordolabs.data_bridge.DaggerDataComponent
import com.ordolabs.thecolor.di.AppComponent
import com.ordolabs.thecolor.di.DaggerAppComponent

internal class TheColorApplication : Application() {

    val appComponent: AppComponent
        get() = requireNotNull(_appComponent) { "AppComponent is not initialized yet" }

    private var _appComponent: AppComponent? = null

    override fun onCreate() {
        super.onCreate()
        setDI()
    }

    // region DI

    private fun setDI() {
        val dataComponent = DaggerDataComponent
            .builder()
            .build()
        val appComponentDependencies = AppComponentDependenciesImpl()
        this._appComponent = DaggerAppComponent
            .builder()
            .dataComponent(dataComponent)
            .dependencies(appComponentDependencies)
            .build()
    }

    // endregion

    private inner class AppComponentDependenciesImpl : AppComponent.Dependencies {

        override val applicationContext: Context = this@TheColorApplication
    }
}