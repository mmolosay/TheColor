package com.ordolabs.thecolor.ui.adapter.pager

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ordolabs.domain.model.ColorModel
import com.ordolabs.thecolor.R
import com.ordolabs.thecolor.ui.adapter.EnumTab
import com.ordolabs.thecolor.ui.fragment.colorinput.ColorInputHexFragment
import com.ordolabs.thecolor.ui.fragment.colorinput.ColorInputRgbFragment
import com.ordolabs.thecolor.util.ext.getEnumSize
import com.ordolabs.thecolor.util.ext.getFromEnum

class ColorInputPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun createFragment(position: Int): Fragment =
        getFromEnum<Tab>(position).getFragmentNewInstance()

    override fun getItemCount(): Int = getEnumSize<ColorModel>()

    enum class Tab(
        @StringRes val titleRes: Int,
        val colorModel: ColorModel
    ) : EnumTab {

        HEX(
            R.string.color_input_hex_hint,
            ColorModel.HEX
        ) {
            override fun getFragmentNewInstance() = ColorInputHexFragment.newInstance()
        },
        RGB(
            R.string.color_input_rgb_hint,
            ColorModel.RGB
        ) {
            override fun getFragmentNewInstance() = ColorInputRgbFragment.newInstance()
        }
    }

}