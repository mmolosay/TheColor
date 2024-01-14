package com.ordolabs.feature_home.viewmodel.color.data

import androidx.lifecycle.viewModelScope
import com.ordolabs.feature_home.ui.adapter.pager.ColorDataPagerAdapter
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.presentation.util.MutableCommandFlow
import io.github.mmolosay.presentation.util.ext.asCommand
import io.github.mmolosay.presentation.util.ext.setSuccess
import io.github.mmolosay.presentation.viewmodel.BaseViewModel
import javax.inject.Inject

@HiltViewModel
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