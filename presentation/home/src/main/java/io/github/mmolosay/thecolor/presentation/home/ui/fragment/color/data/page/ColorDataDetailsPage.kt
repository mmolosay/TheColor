package io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.data.page

import androidx.fragment.app.Fragment
import io.github.mmolosay.thecolor.presentation.home.R
import io.github.mmolosay.thecolor.presentation.home.ui.adapter.pager.ColorDataPagerAdapter
import io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.data.details.ColorDetailsObtainFragment
import io.github.mmolosay.thecolor.presentation.home.ui.fragment.color.data.page.base.BaseColorDataPage

class ColorDataDetailsPage : BaseColorDataPage() {

    // region IColorDataPage

    override val page = ColorDataPagerAdapter.Page.DETAILS

    override fun makeColorDataFragmentNewInstance(): Fragment =
        ColorDetailsObtainFragment.newInstance(details = null)

    override fun getChangePageBtnText(): String =
        resources.getString(R.string.color_data_details_page_change_page_btn)

    // endregion

    companion object {
        fun newInstance() =
            ColorDataDetailsPage()
    }
}