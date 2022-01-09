package com.ordolabs.feature_home.ui.fragment.colordata.page

import androidx.fragment.app.Fragment
import com.ordolabs.feature_home.R
import com.ordolabs.feature_home.ui.adapter.pager.ColorDataPagerAdapter
import com.ordolabs.feature_home.ui.fragment.colordata.ColorDataDetailsFragment
import com.ordolabs.feature_home.ui.fragment.colordata.page.base.BaseColorDataPage
import com.ordolabs.thecolor.model.ColorDetailsPresentation

class ColorDataDetailsPage : BaseColorDataPage<ColorDetailsPresentation>() {

    private fun onColorDetailsSuccess(details: ColorDetailsPresentation) {
//        info.hexClean?.let {
//            // update colorPreview if exact color was fetched
//            val preview = colorInputVM.colorPreview.value.getOrNull()
//            if (preview?.color?.hex == it) return@let
//            val color = ColorUtil.Color(hex = it)
//            val new = ColorInputViewModel.ColorPreview(color, isUserInput = false)
//            colorInputVM.updateColorPreview(new)
//        }
    }

    // region IColorDataPage

    override val page = ColorDataPagerAdapter.Page.DETAILS
    override val data by lazy { colorDataVM.details }

    override fun makeColorDataFragmentNewInstance(): Fragment =
        ColorDataDetailsFragment.newInstance(colorDetails = null)

    override fun retryDataFetch() {
        color?.let { color ->
            colorDataVM.fetchColorDetails(color)
        }
    }

    override fun getChangePageBtnText(): String =
        resources.getString(R.string.color_data_details_page_change_page_btn)

    // endregion

    companion object {
        fun newInstance() =
            ColorDataDetailsPage()
    }
}