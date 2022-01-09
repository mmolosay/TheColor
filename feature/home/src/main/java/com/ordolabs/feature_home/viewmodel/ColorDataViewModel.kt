package com.ordolabs.feature_home.viewmodel

import androidx.lifecycle.viewModelScope
import com.ordolabs.domain.usecase.remote.GetColorInformationBaseUseCase
import com.ordolabs.feature_home.ui.adapter.pager.ColorDataPagerAdapter
import com.ordolabs.thecolor.mapper.toPresentation
import com.ordolabs.thecolor.model.ColorInformationPresentation
import com.ordolabs.thecolor.util.ColorUtil.Color
import com.ordolabs.thecolor.util.MutableStateResourceFlow
import com.ordolabs.thecolor.util.ext.catchFailureIn
import com.ordolabs.thecolor.util.ext.setEmpty
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
import kotlinx.coroutines.flow.collect

// TODO: check and rename all color data names to display their
//  purpose properly after inplementing schemes feature.
class ColorDataViewModel(
    private val getColorInformationUseCase: GetColorInformationBaseUseCase
) : BaseViewModel() {

    private val _information =
        MutableStateResourceFlow<ColorInformationPresentation>(Resource.empty())
    val information = _information.asStateFlow()

    private val _changePageCommand: MutableStateFlow<Resource<ColorDataPagerAdapter.Page>>
    val changePageCommand: SharedFlow<Resource<ColorDataPagerAdapter.Page>>

    private var fetchColorInformationJob: Job? = null

    init {
        _changePageCommand = MutableStateResourceFlow(Resource.empty())
        changePageCommand = _changePageCommand.shareOnceIn(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        fetchColorInformationJob?.cancel()
    }

    fun fetchColorInformation(color: Color) = launch {
        restartFetchingColorInformation().join()
        fetchColorInformationJob = launchInIO {
            getColorInformationUseCase.invoke(color.hex)
                .catchFailureIn(_information)
                .collect { colorInfo ->
                    val info = colorInfo.toPresentation()
                    _information.setSuccess(info)
                }
        }
    }

    fun changeDataPage(dest: ColorDataPagerAdapter.Page) {
        _changePageCommand.setSuccess(dest)
    }

    fun clearColorInformation() {
        _information.setEmpty()
    }

    private fun restartFetchingColorInformation() = launchInMain {
        fetchColorInformationJob?.cancel()
        _information.setLoading()
    }
}