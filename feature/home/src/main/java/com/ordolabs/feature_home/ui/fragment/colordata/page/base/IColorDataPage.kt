package com.ordolabs.feature_home.ui.fragment.colordata.page.base

import androidx.fragment.app.Fragment
import com.ordolabs.feature_home.ui.adapter.pager.ColorDataPagerAdapter

interface IColorDataPage {

    val page: ColorDataPagerAdapter.Page

    fun getContentFragmentNewInstance(): Fragment
    fun getChangePageBtnText(): String
}