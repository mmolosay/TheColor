package io.github.mmolosay.thecolor.presentation.home.viewmodel

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.presentation.center.ColorCenterViewModel
import io.github.mmolosay.thecolor.presentation.common.viewmodel.ViewModelCoroutineScope
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsEventHandler
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsState
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsViewModel
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeEventHandler
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeViewModel
import io.github.mmolosay.thecolor.utils.Store
import kotlinx.coroutines.CoroutineScope
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
    ) {
        disposeComponents() // dispose of current components if there are any
        this.components = factory.create(
            viewModelScope = viewModelScope,
            colorDetailsEventHandler = colorDetailsEventHandler,
            colorSchemeEventHandler = colorSchemeEventHandler,
            selectedSwatchColorDetailsEventHandler = selectedSwatchColorDetailsEventHandler,
        )
    }

    @Synchronized
    fun disposeComponents() {
        val components = components ?: return
        components.colorCenterViewModel.dispose() // will also dispose of its child ViewModels
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
    private val colorDetailsViewModelFactory: ColorDetailsViewModel.Factory,
    private val colorSchemeViewModelFactory: ColorSchemeViewModel.Factory,
    private val colorCenterViewModelFactory: ColorCenterViewModel.Factory,
) {

    fun create(
        viewModelScope: CoroutineScope,
        colorDetailsEventHandler: ColorDetailsEventHandler,
        colorSchemeEventHandler: ColorSchemeEventHandler,
        selectedSwatchColorDetailsEventHandler: ColorDetailsEventHandler,
    ): ColorCenterComponents {
        val colorSchemeViewModelCoroutineScope: CoroutineScope
        val colorCenterViewModel = run {
            val coroutineScope = ViewModelCoroutineScope(parent = viewModelScope)
            val colorDetailsViewModel = colorDetailsViewModelFactory.create(
                coroutineScope = ViewModelCoroutineScope(parent = coroutineScope),
                store = Store(ColorDetailsState.Idle),
                eventHandler = colorDetailsEventHandler,
            )
            colorSchemeViewModelCoroutineScope = ViewModelCoroutineScope(parent = coroutineScope)
            val colorSchemeViewModel = colorSchemeViewModelFactory.create(
                coroutineScope = colorSchemeViewModelCoroutineScope,
                eventHandler = colorSchemeEventHandler,
            )
            return@run colorCenterViewModelFactory.create(
                coroutineScope = coroutineScope,
                colorDetailsViewModel = colorDetailsViewModel,
                colorSchemeViewModel = colorSchemeViewModel,
            )
        }
        val selectedSwatchColorDetailsViewModel = colorDetailsViewModelFactory.create(
            coroutineScope = ViewModelCoroutineScope(parent = colorSchemeViewModelCoroutineScope),
            store = Store(ColorDetailsState.Idle),
            eventHandler = selectedSwatchColorDetailsEventHandler,
        )
        return ColorCenterComponents(
            colorCenterViewModel = colorCenterViewModel,
            selectedSwatchColorDetailsViewModel = selectedSwatchColorDetailsViewModel,
        )
    }
}

/**
 * A [ColorCenterViewModel] with the dependencies that it needs to be created via factory.
 * Once old [ColorCenterViewModel] is no longer needed, it will be disposed.
 * New components (and thus new ViewModel) will be created and used.
 */
/* private for HomeViewModel */
data class ColorCenterComponents(
    val colorCenterViewModel: ColorCenterViewModel,
    val selectedSwatchColorDetailsViewModel: ColorDetailsViewModel,
)