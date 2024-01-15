package io.github.mmolosay.thecolor.presentation.home.viewmodel.color.data

import androidx.lifecycle.viewModelScope
import io.github.mmolosay.thecolor.presentation.home.ui.adapter.pager.ColorDataPagerAdapter
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.presentation.util.ext.shareOnceIn
import io.github.mmolosay.thecolor.utils.Resource
import io.github.mmolosay.thecolor.utils.empty
import io.github.mmolosay.thecolor.utils.success
import io.github.mmolosay.thecolor.presentation.viewmodel.BaseViewModel
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

    fun changeDataPage(dest: io.github.mmolosay.thecolor.presentation.home.ui.adapter.pager.ColorDataPagerAdapter.Page) {
        _changePageCommand.value = Resource.success(dest)
    }
}