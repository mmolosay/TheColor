package io.github.mmolosay.thecolor.presentation.center

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ColorCenterViewModel @Inject constructor() : ViewModel() {

    private val _dataFlow = MutableStateFlow(initialData())
    val dataFlow = _dataFlow.asStateFlow()

    private fun changePage(destPage: Int) {
        _dataFlow.update { data ->
            data.copy(page = destPage)
        }
    }

    private fun onPageChanged(newPage: Int) {
        _dataFlow.update { data ->
            data.copy(page = newPage)
        }
    }

    private fun initialData(): ColorCenterData =
        ColorCenterData(
            page = 0,
            changePage = ::changePage, // curious thing that functional interfaces allow to do
            onPageChanged = ::onPageChanged,
        )
}