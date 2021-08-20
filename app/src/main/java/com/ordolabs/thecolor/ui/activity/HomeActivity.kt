package com.ordolabs.thecolor.ui.activity

import by.kirich1409.viewbindingdelegate.viewBinding
import com.ordolabs.thecolor.R
import com.ordolabs.thecolor.databinding.ActivityHomeBinding
import com.ordolabs.thecolor.ui.fragment.colorinput.ColorInputHostFragment
import com.ordolabs.thecolor.util.ext.setFragment
import com.ordolabs.thecolor.util.setTransparentSystemBars
import com.ordolabs.thecolor.viewmodel.ColorInputViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeActivity : BaseActivity(R.layout.activity_home) {

    private val binding: ActivityHomeBinding by viewBinding()

    override fun setUp() {
        setTransparentSystemBars()
    }

    override fun setViews() {
        setColorInputFragment()
    }

    private fun setColorInputFragment() {
        val fragment = ColorInputHostFragment.newInstance()
        setFragment(fragment)
    }
}