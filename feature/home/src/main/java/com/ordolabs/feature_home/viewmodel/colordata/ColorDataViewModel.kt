package com.ordolabs.feature_home.viewmodel.colordata

import androidx.lifecycle.viewModelScope
import com.ordolabs.feature_home.ui.adapter.pager.ColorDataPagerAdapter
import com.ordolabs.thecolor.util.ColorUtil.Color
import com.ordolabs.thecolor.util.MutableStateResourceFlow
import com.ordolabs.thecolor.util.ext.setLoading
import com.ordolabs.thecolor.util.ext.setSuccess
import com.ordolabs.thecolor.util.ext.shareOnceIn
import com.ordolabs.thecolor.util.struct.Resource
import com.ordolabs.thecolor.util.struct.empty
import com.ordolabs.thecolor.viewmodel.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow

// TODO: check and rename all color data names to display their
//  purpose properly after inplementing schemes feature.
class ColorDataViewModel : BaseViewModel() {

    private val _scheme =
        MutableStateResourceFlow<Unit>(Resource.empty()) // TODO: type
    val scheme = _scheme.asStateFlow()

    private val _changePageCommand: MutableStateFlow<Resource<ColorDataPagerAdapter.Page>>
    val changePageCommand: SharedFlow<Resource<ColorDataPagerAdapter.Page>>

    private var fetchColorSchemeJob: Job? = null

    init {
        _changePageCommand = MutableStateResourceFlow(Resource.empty())
        changePageCommand = _changePageCommand.shareOnceIn(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        fetchColorSchemeJob?.cancel()
    }

    fun fetchColorScheme(color: Color) = launch {
        restartFetchingColorScheme()
        fetchColorSchemeJob = launchInIO {
            // TODO: impl
        }
    }

    fun changeDataPage(dest: ColorDataPagerAdapter.Page) {
        _changePageCommand.setSuccess(dest)
    }

    private fun restartFetchingColorScheme() = launchInMain {
        fetchColorSchemeJob?.cancel()
        _scheme.setLoading()
    }
}