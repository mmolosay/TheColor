package io.github.mmolosay.thecolor.presentation.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _dataFlow = MutableStateFlow(initialData())
    val dataFlow = _dataFlow.asStateFlow()

    private val _navEventFlow = MutableStateFlow<SettingsNavEvent?>(null)
    val navEventFlow = _navEventFlow.asStateFlow()

    private fun setGoToHomeNavEvent() {
        val event = SettingsNavEvent.GoToHome(onConsumed = ::clearNavEvent)
        _navEventFlow.value = event
    }

    private fun clearNavEvent() {
        _navEventFlow.value = null
    }

    private fun initialData() =
        SettingsData(
            goToHome = ::setGoToHomeNavEvent,
        )
}