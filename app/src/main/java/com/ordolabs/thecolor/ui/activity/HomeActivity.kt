package com.ordolabs.thecolor.ui.activity

import com.ordolabs.thecolor.databinding.ActivityHomeBinding
import com.ordolabs.thecolor.util.setTransparentSystemBars

class HomeActivity : BaseActivity<ActivityHomeBinding>() {

    override fun setUp() {
        setTransparentSystemBars()
    }

    override fun setViews() {
    }

    override fun getViewBinding() = ActivityHomeBinding.inflate(layoutInflater)
}