package com.ordolabs.feature_home.ui.fragment.color.input

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.databinding.ColorInputPagerFragmentBinding
import com.ordolabs.feature_home.ui.adapter.pager.ColorInputPagerAdapter
import com.ordolabs.feature_home.ui.fragment.BaseFragment
import com.ordolabs.thecolor.ui.util.itemdecoration.MarginDecoration
import com.ordolabs.thecolor.util.ext.getFromEnumOrNull
import com.ordolabs.thecolor.R as RApp

/**
 * `Fragment` with `ViewPager` which contains `Fragment`s of specific color scheme inputs.
 */
class ColorInputPagerFragment :
    BaseFragment() {

    private val binding: ColorInputPagerFragmentBinding by viewBinding()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.color_input_pager_fragment, container, false)
    }

    // region Set views

    override fun setViews() {
        setViewPager()
        setTabs()
    }

    private fun setViewPager() = binding.run {
        val adapter = ColorInputPagerAdapter(this@ColorInputPagerFragment)
        val decoration = MarginDecoration.Horizontal(
            resources,
            RApp.dimen.offset_content_horizontal
        )
        pager.adapter = adapter
        pager.offscreenPageLimit = adapter.itemCount
        pager.addItemDecoration(decoration)
    }

    private fun setTabs() = binding.run {
        TabLayoutMediator(tabs, pager, ::configureInputTab).attach()
    }

    private fun configureInputTab(tab: TabLayout.Tab, position: Int) {
        val page = getFromEnumOrNull<ColorInputPagerAdapter.Page>(position) ?: return
        tab.setText(page.titleRes)
    }

    // endregion

    companion object {
        fun newInstance() = ColorInputPagerFragment()
    }
}