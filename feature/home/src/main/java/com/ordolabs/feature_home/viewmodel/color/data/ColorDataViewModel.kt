package com.ordolabs.feature_home.viewmodel.color.data

import androidx.lifecycle.viewModelScope
import com.ordolabs.feature_home.ui.adapter.pager.ColorDataPagerAdapter
import com.ordolabs.thecolor.util.MutableCommandFlow
import com.ordolabs.thecolor.util.ext.asCommand
import com.ordolabs.thecolor.util.ext.setSuccess
import com.ordolabs.thecolor.viewmodel.BaseViewModel
import javax.inject.Inject

class ColorDataViewModel @Inject constructor() : BaseViewModel() {

    private val _changePageCommand = MutableCommandFlow<ColorDataPagerAdapter.Page>()
    val changePageCommand = _changePageCommand.asCommand(viewModelScope)

    override fun onCleared() {
        super.onCleared()
    }

    fun changeDataPage(dest: ColorDataPagerAdapter.Page) {
        _changePageCommand.setSuccess(dest)
    }
}