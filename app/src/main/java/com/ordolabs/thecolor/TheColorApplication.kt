package com.ordolabs.thecolor

import android.app.Application
import com.ordolabs.core.di.DaggerCoreComponent
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
        val coreComponent = DaggerCoreComponent
            .builder()
            .applicationContext(this)
            .build()
        this._appComponent = DaggerAppComponent
            .builder()
            .coreComponent(coreComponent)
            .build()
    }

    // endregion
}