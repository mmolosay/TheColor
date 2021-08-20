package com.ordolabs.thecolor.ui.fragment

import by.kirich1409.viewbindingdelegate.viewBinding
import com.ordolabs.thecolor.R
import com.ordolabs.thecolor.databinding.FragmentHomeBinding
import com.ordolabs.thecolor.viewmodel.HomeViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : BaseFragment(R.layout.fragment_home) {

    private val binding: FragmentHomeBinding by viewBinding()
    private val homeVM: HomeViewModel by viewModel()

    override fun setUp() {
        // nothing is here
    }

    override fun setViews() {
        // nothing is here
    }
}