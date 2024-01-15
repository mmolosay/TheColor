package io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.data.page

import androidx.fragment.app.Fragment
import io.github.mmolosay.thecolor.presentation.home.R
import io.github.mmolosay.thecolor.presentation.home.ui.adapter.pager.ColorDataPagerAdapter
import io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.data.page.base.BaseColorDataPage
import io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.data.scheme.ColorSchemeObtainFragment

class ColorDataSchemePage : BaseColorDataPage() {

    // region IColorDataPage

    override val page = io.github.mmolosay.thecolor.presentation.home.ui.adapter.pager.ColorDataPagerAdapter.Page.SCHEME

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