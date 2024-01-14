package com.ordolabs.feature_home.ui.adapter.pager

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.ui.fragment.color.input.page.ColorInputHexFragment
import com.ordolabs.feature_home.ui.fragment.color.input.page.ColorInputRgbFragment
import io.github.mmolosay.presentation.ui.adapter.EnumFragmentPage
import io.github.mmolosay.presentation.util.ext.getEnumSize
import io.github.mmolosay.presentation.util.ext.getFromEnum

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
            R.string.color_hex_label,
        ) {
            override fun getFragmentNewInstance(): Fragment =
                ColorInputHexFragment.newInstance()
        },
        RGB(
            R.string.color_rgb_label,
        ) {
            override fun getFragmentNewInstance(): Fragment =
                ColorInputRgbFragment.newInstance()
        }
    }

}