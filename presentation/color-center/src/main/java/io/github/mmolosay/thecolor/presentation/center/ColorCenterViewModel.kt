package io.github.mmolosay.thecolor.presentation.center

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.presentation.center.ColorCenterData.ChangePageEvent
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
            val event = ChangePageEvent(
                destPage = destPage,
                onConsumed = ::clearChangePageEvent,
            )
            data.copy(changePageEvent = event)
        }
    }

    private fun clearChangePageEvent() {
        _dataFlow.update { data ->
            data.copy(changePageEvent = null)
        }
    }

    private fun initialData(): ColorCenterData =
        ColorCenterData(
            changePage = ::changePage,
            changePageEvent = null,
        )
}