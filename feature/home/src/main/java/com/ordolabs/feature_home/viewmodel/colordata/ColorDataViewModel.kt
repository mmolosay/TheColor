package com.ordolabs.feature_home.viewmodel.colordata

import androidx.lifecycle.viewModelScope
import com.ordolabs.feature_home.ui.adapter.pager.ColorDataPagerAdapter
import com.ordolabs.thecolor.util.MutableStateResourceFlow
import com.ordolabs.thecolor.util.ext.setSuccess
import com.ordolabs.thecolor.util.ext.shareOnceIn
import com.ordolabs.thecolor.util.struct.Resource
import com.ordolabs.thecolor.util.struct.empty
import com.ordolabs.thecolor.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow

class ColorDataViewModel : BaseViewModel() {

    private val _changePageCommand: MutableStateFlow<Resource<ColorDataPagerAdapter.Page>>
    val changePageCommand: SharedFlow<Resource<ColorDataPagerAdapter.Page>>

    init {
        _changePageCommand = MutableStateResourceFlow(Resource.empty())
        changePageCommand = _changePageCommand.shareOnceIn(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
    }

    fun changeDataPage(dest: ColorDataPagerAdapter.Page) {
        _changePageCommand.setSuccess(dest)
    }
}