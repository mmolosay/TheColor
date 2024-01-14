package com.ordolabs.thecolor.ui.activity

import by.kirich1409.viewbindingdelegate.viewBinding
import com.ordolabs.thecolor.R
import com.ordolabs.thecolor.databinding.ActivityHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : BaseActivity(R.layout.activity_home) {

    private val binding: ActivityHomeBinding by viewBinding()

    override fun setUp() {
        // nothing is here
    }

    override fun setViews() {
        // nothing is here
    }
}