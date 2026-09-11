package io.github.mmolosay.thecolor.presentation.home.viewmodel

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.presentation.center.ColorCenterDataFactory
import io.github.mmolosay.thecolor.presentation.center.ColorCenterViewModel
import io.github.mmolosay.thecolor.presentation.common.viewmodel.ViewModelCoroutineScope
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsEventHandler
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsState
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsViewModel
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeEventHandler
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeState
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

/**
 * Stores [ColorCenterComponents].
 * Provides methods for disposing of a current components when they are no longer needed
 * and for creating new components.
 */
/* private for HomeViewModel */
class ColorCenterComponentsStore @AssistedInject constructor(
    @Assisted private val viewModelScope: CoroutineScope,
    private val factory: ColorCenterComponentsFactory,
) {
    @Volatile // faster than '@Synchronized get'
    var components: ColorCenterComponents? = null
        private set

    @Synchronized
    fun createNewComponents(
        colorDetailsEventHandler: ColorDetailsEventHandler,
        colorSchemeEventHandler: ColorSchemeEventHandler,
        selectedSwatchColorDetailsEventHandler: ColorDetailsEventHandler,
    ): ColorCenterComponents {
        disposeComponents() // dispose of current components if there are any
        val components = factory.create(
            viewModelScope = viewModelScope,
            colorDetailsEventHandler = colorDetailsEventHandler,
            colorSchemeEventHandler = colorSchemeEventHandler,
            selectedSwatchColorDetailsEventHandler = selectedSwatchColorDetailsEventHandler,
        )
        this.components = components
        return components
    }

    @Synchronized
    fun disposeComponents() {
        val components = components ?: return
        components.colorCenterViewModel.dispose()
        components.colorDetailsViewModel.dispose()
        components.colorSchemeViewModel.dispose()
        components.selectedSwatchColorDetailsViewModel.dispose()
        this.components = null
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            viewModelScope: CoroutineScope,
        ): ColorCenterComponentsStore
    }
}

/* private for ColorCenterComponentsStore */
class ColorCenterComponentsFactory @Inject constructor(
    private val colorCenterDataFactory: ColorCenterDataFactory,
    private val colorCenterViewModelFactory: ColorCenterViewModel.Factory,
    private val colorDetailsViewModelFactory: ColorDetailsViewModel.Factory,
    private val colorSchemeViewModelFactory: ColorSchemeViewModel.Factory,
) {

    fun create(
        viewModelScope: CoroutineScope,
        colorDetailsEventHandler: ColorDetailsEventHandler,
        colorSchemeEventHandler: ColorSchemeEventHandler,
        selectedSwatchColorDetailsEventHandler: ColorDetailsEventHandler,
    ): ColorCenterComponents {
        val colorCenterViewModel = colorCenterViewModelFactory.create(
            coroutineScope = ViewModelCoroutineScope(parent = viewModelScope),
            dataFlow = MutableStateFlow(colorCenterDataFactory.create()),
        )
        val colorDetailsViewModel = colorDetailsViewModelFactory.create(
            coroutineScope = ViewModelCoroutineScope(parent = viewModelScope),
            stateFlow = MutableStateFlow(ColorDetailsState.Idle),
            eventHandler = colorDetailsEventHandler,
        )
        val colorSchemeViewModel = colorSchemeViewModelFactory.create(
            coroutineScope = ViewModelCoroutineScope(parent = viewModelScope),
            stateFlow = MutableStateFlow(ColorSchemeState.Idle),
            eventHandler = colorSchemeEventHandler,
        )
        val selectedSwatchColorDetailsViewModel = colorDetailsViewModelFactory.create(
            coroutineScope = ViewModelCoroutineScope(parent = viewModelScope),
            stateFlow = MutableStateFlow(ColorDetailsState.Idle),
            eventHandler = selectedSwatchColorDetailsEventHandler,
        )
        return ColorCenterComponents(
            colorCenterViewModel = colorCenterViewModel,
            colorDetailsViewModel = colorDetailsViewModel,
            colorSchemeViewModel = colorSchemeViewModel,
            selectedSwatchColorDetailsViewModel = selectedSwatchColorDetailsViewModel,
        )
    }
}

/**
 * The ViewModels of one 'Color Center' session.
 * When the session ends, they are disposed and a new set is created for the next one.
 */
/* private for HomeViewModel */
data class ColorCenterComponents(
    val colorCenterViewModel: ColorCenterViewModel,
    val colorDetailsViewModel: ColorDetailsViewModel,
    val colorSchemeViewModel: ColorSchemeViewModel,
    val selectedSwatchColorDetailsViewModel: ColorDetailsViewModel,
)