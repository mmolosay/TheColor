package com.ordolabs.feature_home.viewmodel.color.data

import androidx.lifecycle.viewModelScope
import com.ordolabs.feature_home.ui.adapter.pager.ColorDataPagerAdapter
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.presentation.util.ext.shareOnceIn
import com.ordolabs.util.Resource
import com.ordolabs.util.empty
import com.ordolabs.util.success
import io.github.mmolosay.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class ColorDataViewModel @Inject constructor() : BaseViewModel() {

    private val _changePageCommand =
        MutableStateFlow<Resource<ColorDataPagerAdapter.Page>>(Resource.empty())
    val changePageCommand = _changePageCommand.shareOnceIn(viewModelScope)

    override fun onCleared() {
        super.onCleared()
    }

    fun changeDataPage(dest: ColorDataPagerAdapter.Page) {
        _changePageCommand.value = Resource.success(dest)
    }
}