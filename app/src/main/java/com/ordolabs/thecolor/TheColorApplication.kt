package com.ordolabs.thecolor

import android.app.Application
import android.content.Context
import com.ordolabs.thecolor.di.AppComponent
//import com.ordolabs.core.di.DaggerCoreComponent

internal class TheColorApplication : Application() {

    val appComponent: AppComponent
        get() = requireNotNull(_appComponent) { "AppComponent is not initialized yet" }

    private var _appComponent: AppComponent? = null

    override fun onCreate() {
        super.onCreate()
        setDagger()
    }

    private fun setDagger() {
//        val a = DaggerCoreComponent.create()
        val appComponentDependencies = AppComponentDependenciesImpl()
//        this._appComponent = Da
//            .builder()
//            .dependencies(appComponentDependencies)
//            .build()
    }

    private inner class AppComponentDependenciesImpl : AppComponent.Dependencies {

        override val applicationContext: Context = this@TheColorApplication
    }
}