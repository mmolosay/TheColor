package com.ordolabs.feature_home.ui.adapter.pager

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ordolabs.feature_home.ui.fragment.colordata.page.ColorDataDetailsPage
import com.ordolabs.feature_home.ui.fragment.colordata.page.ColorDataSchemePage
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
                ColorDataDetailsPage.newInstance()
        },
        SCHEME {
            override fun getFragmentNewInstance(): Fragment =
                ColorDataSchemePage.newInstance()
        }
    }
}