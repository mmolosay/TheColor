package com.ordolabs.feature_home.ui.adapter.pager

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ordolabs.feature_home.ui.fragment.colordata.ColorDataDetailsFragment
import com.ordolabs.feature_home.ui.fragment.colordata.ColorDataSchemeFragment
import com.ordolabs.thecolor.ui.adapter.EnumFragmentPage
import com.ordolabs.thecolor.util.ext.getEnumSize
import com.ordolabs.thecolor.util.ext.getFromEnum

class ColorDataPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun createFragment(position: Int): Fragment =
        getFromEnum<Page>(position).getFragmentNewInstance()

    override fun getItemCount(): Int =
        getEnumSize<Page>()

    @Suppress("unused")
    enum class Page : EnumFragmentPage {

        DETAILS {
            override fun getFragmentNewInstance(): Fragment =
                ColorDataDetailsFragment.newInstance()
        },
        SCHEME {
            override fun getFragmentNewInstance(): Fragment =
                ColorDataSchemeFragment.newInstance()
        }
    }
}