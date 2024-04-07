package io.github.mmolosay.thecolor.presentation.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _dataFlow = MutableStateFlow(initialData())
    val dataFlow = _dataFlow.asStateFlow()

    private fun setGoToHomeNavEvent() {
        _dataFlow.update {
            val event = SettingsData.NavEvent.GoToHome(onConsumed = ::clearNavEvent)
            it.copy(navEvent = event)
        }
    }

    private fun clearNavEvent() {
        _dataFlow.update {
            it.copy(navEvent = null)
        }
    }

    private fun initialData() =
        SettingsData(
            goToHome = ::setGoToHomeNavEvent,
            navEvent = null,
        )
}