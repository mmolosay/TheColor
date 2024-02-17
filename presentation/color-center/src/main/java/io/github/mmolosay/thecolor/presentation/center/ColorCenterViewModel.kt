package io.github.mmolosay.thecolor.presentation.center

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.presentation.center.ColorCenterData.ChangePageAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ColorCenterViewModel @Inject constructor() : ViewModel() {

    private val changePage = makeChangePageAction()

    private val _dataFlow = MutableStateFlow(initialData())
    val dataFlow = _dataFlow.asStateFlow()

    private fun makeChangePageAction() =
        ChangePageAction { destPageIndex ->
            _dataFlow.update { data ->
                data.copy(pageIndex = destPageIndex)
            }
        }

    private fun initialData(): ColorCenterData =
        ColorCenterData(
            pageIndex = 0,
            changePage = changePage,
        )
}