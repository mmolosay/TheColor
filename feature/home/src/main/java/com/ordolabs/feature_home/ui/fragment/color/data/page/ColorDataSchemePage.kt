package com.ordolabs.feature_home.ui.fragment.color.data.page

import androidx.fragment.app.Fragment
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.ui.adapter.pager.ColorDataPagerAdapter
import com.ordolabs.feature_home.ui.fragment.color.data.page.base.BaseColorDataPage
import com.ordolabs.feature_home.ui.fragment.color.data.scheme.ColorSchemeObtainFragment

class ColorDataSchemePage : BaseColorDataPage() {

    // region IColorDataPage

    override val page = ColorDataPagerAdapter.Page.SCHEME

    override fun makeColorDataFragmentNewInstance(): Fragment =
        ColorSchemeObtainFragment.newInstance()

    override fun getChangePageBtnText(): String =
        resources.getString(R.string.color_data_scheme_page_change_page_btn)

    // endregion


    companion object {
        fun newInstance() =
            ColorDataSchemePage()
    }
}