package io.github.mmolosay.thecolor.presentation.center

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.presentation.api.SimpleViewModel
import io.github.mmolosay.thecolor.presentation.center.ColorCenterData.ChangePageEvent
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsViewModel
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Handles presentation logic of the 'Color Center' feature.
 *
 * Unlike typical `ViewModel`s, it doesn't derive from Google's [ViewModel][androidx.lifecycle.ViewModel],
 * thus cannot be instantiated using [ViewModelProvider][androidx.lifecycle.ViewModelProvider].
 *
 * Instead, it can be created within "simple" `ViewModel` or Google's `ViewModel`.
 */
class ColorCenterViewModel @AssistedInject constructor(
    @Assisted coroutineScope: CoroutineScope,
    @Assisted val colorDetailsViewModel: ColorDetailsViewModel,
    @Assisted val colorSchemeViewModel: ColorSchemeViewModel,
) : SimpleViewModel(coroutineScope) {

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

    override fun dispose() {
        super.dispose()
        colorDetailsViewModel.dispose()
        colorSchemeViewModel.dispose()
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            coroutineScope: CoroutineScope,
            colorDetailsViewModel: ColorDetailsViewModel,
            colorSchemeViewModel: ColorSchemeViewModel,
        ): ColorCenterViewModel
    }
}