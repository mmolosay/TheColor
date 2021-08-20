package com.ordolabs.thecolor.ui.activity

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity(@LayoutRes layoutRes: Int) : AppCompatActivity(layoutRes) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

    companion object {
        // extra keys and stuff
    }
}