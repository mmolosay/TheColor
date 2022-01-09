package com.ordolabs.feature_home.ui.fragment.colordata

import by.kirich1409.viewbindingdelegate.viewBinding
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.ColorDataFragmentBinding
import com.ordolabs.feature_home.ui.adapter.pager.ColorDataPagerAdapter
import com.ordolabs.feature_home.ui.fragment.BaseFragment

class ColorDataFragment : BaseFragment(R.layout.color_data_fragment) {

    private val binding: ColorDataFragmentBinding by viewBinding()

    override fun collectViewModelsData() {
        // nothing is here
    }

    override fun setViews() {
        setViewPager()
    }

    private fun setViewPager() =
        binding.pager.let { pager ->
            val adapter = ColorDataPagerAdapter(this)
            pager.adapter = adapter
        }

    companion object {

        fun newInstance() =
            ColorDataFragment()
    }
}