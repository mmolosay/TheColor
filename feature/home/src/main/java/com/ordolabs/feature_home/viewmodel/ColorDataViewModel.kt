package com.ordolabs.feature_home.viewmodel

import androidx.lifecycle.viewModelScope
import com.ordolabs.domain.usecase.remote.GetColorInformationBaseUseCase
import com.ordolabs.feature_home.ui.adapter.pager.ColorDataPagerAdapter
import com.ordolabs.thecolor.mapper.toPresentation
import com.ordolabs.thecolor.model.ColorDetailsPresentation
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

    private val _details =
        MutableStateResourceFlow<ColorDetailsPresentation>(Resource.empty())
    val details = _details.asStateFlow()

    private val _scheme =
        MutableStateResourceFlow<Unit>(Resource.empty()) // TODO: type
    val scheme = _scheme.asStateFlow()

    private val _changePageCommand: MutableStateFlow<Resource<ColorDataPagerAdapter.Page>>
    val changePageCommand: SharedFlow<Resource<ColorDataPagerAdapter.Page>>

    private var fetchColorDetailsJob: Job? = null
    private var fetchColorSchemeJob: Job? = null

    init {
        _changePageCommand = MutableStateResourceFlow(Resource.empty())
        changePageCommand = _changePageCommand.shareOnceIn(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        fetchColorDetailsJob?.cancel()
        fetchColorSchemeJob?.cancel()
    }

    fun fetchColorData(color: Color) {
        fetchColorDetails(color)
        fetchColorScheme(color)
    }

    fun fetchColorDetails(color: Color) = launch {
        restartFetchingColorInformation().join()
        fetchColorDetailsJob = launchInIO {
            getColorInformationUseCase.invoke(color.hex)
                .catchFailureIn(_details)
                .collect { colorInfo ->
                    val info = colorInfo.toPresentation()
                    _details.setSuccess(info)
                }
        }
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

    fun clearColorInformation() {
        _details.setEmpty()
    }

    private fun restartFetchingColorInformation() = launchInMain {
        fetchColorDetailsJob?.cancel()
        _details.setLoading()
    }

    private fun restartFetchingColorScheme() = launchInMain {
        fetchColorSchemeJob?.cancel()
        _scheme.setLoading()
    }
}