package com.ordolabs.feature_home.ui.fragment.colordata.page.base

import androidx.fragment.app.Fragment
import com.ordolabs.feature_home.ui.adapter.pager.ColorDataPagerAdapter

/**
 * Interface for color data page fragment.
 */
interface IColorDataPage {

    val page: ColorDataPagerAdapter.Page

    fun makeColorDataFragmentNewInstance(): Fragment
    fun getChangePageBtnText(): String
}