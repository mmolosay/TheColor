package com.ordolabs.feature_home.ui.fragment.colordata.page.base

import androidx.fragment.app.Fragment
import com.ordolabs.feature_home.ui.adapter.pager.ColorDataPagerAdapter
import com.ordolabs.thecolor.util.struct.Resource
import kotlinx.coroutines.flow.Flow

interface IColorDataPage<D> {

    val page: ColorDataPagerAdapter.Page
    val data: Flow<Resource<D>>

    fun makeColorDataFragmentNewInstance(): Fragment
    fun retryDataFetch()
    fun getChangePageBtnText(): String
}