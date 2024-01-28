package io.github.mmolosay.thecolor.presentation.home.ui.adapter.pager

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.data.page.ColorDataDetailsPage
import io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.data.page.ColorDataSchemePage
import io.github.mmolosay.thecolor.presentation.ui.adapter.EnumFragmentPage
import io.github.mmolosay.thecolor.presentation.util.ext.getEnumSize
import io.github.mmolosay.thecolor.presentation.util.ext.getFromEnum

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