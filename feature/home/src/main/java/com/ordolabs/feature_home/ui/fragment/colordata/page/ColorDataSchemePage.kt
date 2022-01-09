package com.ordolabs.feature_home.ui.fragment.colordata.page

import androidx.fragment.app.Fragment
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.ui.adapter.pager.ColorDataPagerAdapter
import com.ordolabs.feature_home.ui.fragment.colordata.ColorDataSchemeFragment
import com.ordolabs.feature_home.ui.fragment.colordata.page.base.BaseColorDataPage
import com.ordolabs.thecolor.util.struct.Resource
import kotlinx.coroutines.flow.Flow

class ColorDataSchemePage : BaseColorDataPage<Unit /* TODO: type */>() {

    // region IColorDataPage

    override val page = ColorDataPagerAdapter.Page.SCHEME

    override fun makeColorDataFragmentNewInstance(data: Unit): Fragment =
        ColorDataSchemeFragment.newInstance()

    override fun getPageDataFlow(): Flow<Resource<Unit>> =
        colorDataVM.scheme

    override fun retryDataFetch() {
        color?.let { color ->
            colorDataVM.fetchColorScheme(color)
        }
    }

    override fun getChangePageBtnText(): String =
        resources.getString(R.string.color_data_scheme_page_change_page_btn)

    // endregion


    companion object {
        fun newInstance() =
            ColorDataSchemePage()
    }
}