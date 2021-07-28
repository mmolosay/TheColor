package com.ordolabs.thecolor.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<B : ViewBinding> : AppCompatActivity() {

    protected val b by lazy { getViewBinding() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b // initialization
        setContentView(b.root)

        parseStartIntent()
        setUp()
        setViews()
    }

    /**
     * Parses `Intent`, that started this `Activity`.
     */
    protected open fun parseStartIntent() {
        // override me
    }

    /**
     * Configures non-view components.
     */
    abstract fun setUp()

    /**
     * Sets activity's views and configures them.
     */
    abstract fun setViews()

    abstract fun getViewBinding(): B

    companion object {
        // extra keys and stuff
    }
}