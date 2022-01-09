package com.ordolabs.feature_home.ui.adapter.pager

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.ui.fragment.colorinput.ColorInputHexFragment
import com.ordolabs.feature_home.ui.fragment.colorinput.ColorInputRgbFragment
import com.ordolabs.thecolor.ui.adapter.EnumFragmentPage
import com.ordolabs.thecolor.util.ext.getEnumSize
import com.ordolabs.thecolor.util.ext.getFromEnum

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
            R.string.color_input_hex_hint,
        ) {
            override fun getFragmentNewInstance(): Fragment =
                ColorInputHexFragment.newInstance()
        },
        RGB(
            R.string.color_input_rgb_hint,
        ) {
            override fun getFragmentNewInstance(): Fragment =
                ColorInputRgbFragment.newInstance()
        }
    }

}