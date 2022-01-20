package com.ordolabs.feature_home.ui.adapter.pager

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ordolabs.feature_home.ui.fragment.color.input.ColorInputHexFragment
import com.ordolabs.feature_home.ui.fragment.color.input.ColorInputRgbFragment
import com.ordolabs.thecolor.ui.adapter.EnumFragmentPage
import com.ordolabs.thecolor.util.ext.getEnumSize
import com.ordolabs.thecolor.util.ext.getFromEnum
import com.ordolabs.thecolor.R as RApp

class ColorInputPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun createFragment(position: Int): Fragment =
        getFromEnum<Page>(position).getFragmentNewInstance()

    override fun getItemCount(): Int =
        getEnumSize<Page>()

    @Suppress("unused")
    enum class Page(
        @StringRes val titleRes: Int
    ) : EnumFragmentPage {

        HEX(
            RApp.string.color_hex_label,
        ) {
            override fun getFragmentNewInstance(): Fragment =
                ColorInputHexFragment.newInstance()
        },
        RGB(
            RApp.string.color_rgb_label,
        ) {
            override fun getFragmentNewInstance(): Fragment =
                ColorInputRgbFragment.newInstance()
        }
    }

}