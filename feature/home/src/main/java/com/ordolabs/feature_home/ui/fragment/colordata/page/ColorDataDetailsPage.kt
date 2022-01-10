package com.ordolabs.feature_home.ui.fragment.colordata.page

import androidx.fragment.app.Fragment
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.ui.adapter.pager.ColorDataPagerAdapter
import com.ordolabs.feature_home.ui.fragment.colordata.details.ColorDataDetailsObtainFragment
import com.ordolabs.feature_home.ui.fragment.colordata.page.base.BaseColorDataPage
import com.ordolabs.thecolor.model.ColorDetailsPresentation

class ColorDataDetailsPage : BaseColorDataPage<ColorDetailsPresentation>() {

    // region IColorDataPage

    override val page = ColorDataPagerAdapter.Page.DETAILS

    override fun makeColorDataFragmentNewInstance(): Fragment =
        ColorDataDetailsObtainFragment.newInstance()

    override fun getChangePageBtnText(): String =
        resources.getString(R.string.color_data_details_page_change_page_btn)

    // endregion

    companion object {
        fun newInstance() =
            ColorDataDetailsPage()
    }
}