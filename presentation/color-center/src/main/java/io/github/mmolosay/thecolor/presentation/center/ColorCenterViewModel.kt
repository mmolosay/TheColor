package io.github.mmolosay.thecolor.presentation.center

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.presentation.api.ColorCenterCommandProvider
import io.github.mmolosay.thecolor.presentation.api.ColorCenterEventStore
import io.github.mmolosay.thecolor.presentation.center.ColorCenterData.ChangePageEvent
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsViewModel
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Composed of sub-feature ViewModels of nested Views.
 *
 * Not a ViewModel-ViewModel in terms of Android development.
 * It doesn't derive from [androidx.lifecycle.ViewModel], so should only be used in "real" ViewModels
 * which do derive from Android-aware implementation.
 */
class ColorCenterViewModel @AssistedInject constructor(
    @Assisted coroutineScope: CoroutineScope,
    @Assisted colorCenterCommandProvider: io.github.mmolosay.thecolor.presentation.api.ColorCenterCommandProvider,
    @Assisted colorCenterEventStore: io.github.mmolosay.thecolor.presentation.api.ColorCenterEventStore,
    colorDetailsViewModelFactory: ColorDetailsViewModel.Factory,
    colorSchemeViewModelFactory: ColorSchemeViewModel.Factory,
) {

    private val _dataFlow = MutableStateFlow(initialData())
    val dataFlow = _dataFlow.asStateFlow()

    val colorDetailsViewModel: ColorDetailsViewModel by lazy {
        colorDetailsViewModelFactory.create(
            coroutineScope = coroutineScope,
            colorCenterCommandProvider = colorCenterCommandProvider,
            colorCenterEventStore = colorCenterEventStore,
        )
    }

    val colorSchemeViewModel: ColorSchemeViewModel by lazy {
        colorSchemeViewModelFactory.create(
            coroutineScope = coroutineScope,
            colorCenterCommandProvider = colorCenterCommandProvider,
        )
    }

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

    @AssistedFactory
    fun interface Factory {
        fun create(
            coroutineScope: CoroutineScope,
            colorCenterCommandProvider: io.github.mmolosay.thecolor.presentation.api.ColorCenterCommandProvider,
            colorCenterEventStore: io.github.mmolosay.thecolor.presentation.api.ColorCenterEventStore,
        ): ColorCenterViewModel
    }
}