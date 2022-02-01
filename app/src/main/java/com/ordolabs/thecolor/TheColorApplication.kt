package com.ordolabs.thecolor

import android.app.Application
import com.ordolabs.di.AppComponent
import com.ordolabs.di.DaggerAppComponent

internal class TheColorApplication : Application() {

    val appComponent: AppComponent
        get() = requireNotNull(_appComponent) { "AppComponent is not initialized yet" }

    private var _appComponent: AppComponent? = null

    override fun onCreate() {
        super.onCreate()

        this._appComponent = DaggerAppComponent.create()
    }
}