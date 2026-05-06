package io.github.mmolosay.thecolor.presentation.home.viewmodel

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.presentation.center.ColorCenterViewModel
import io.github.mmolosay.thecolor.presentation.common.viewmodel.ViewModelCoroutineScope
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsEventStore
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsViewModel
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeEventStore
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeViewModel
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Provider

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
    fun createNewComponents() {
        disposeComponents() // dispose of current components if there are any
        this.components = factory.create(viewModelScope)
    }

    @Synchronized
    fun disposeComponents() {
        val components = components ?: return
        components.colorCenterViewModel.dispose() // will also dispose of its child ViewModels
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
    private val colorDetailsEventStoreProvider: Provider<ColorDetailsEventStore>,
    private val colorDetailsViewModelFactory: ColorDetailsViewModel.Factory,

    private val colorSchemeEventStoreProvider: Provider<ColorSchemeEventStore>,
    private val colorSchemeViewModelFactory: ColorSchemeViewModel.Factory,

    private val colorCenterViewModelFactory: ColorCenterViewModel.Factory,
) {

    fun create(viewModelScope: CoroutineScope): ColorCenterComponents {
        val colorCenterViewModelCoroutineScope = ViewModelCoroutineScope(parent = viewModelScope)
        val colorDetailsEventStore = colorDetailsEventStoreProvider.get()
        val colorDetailsViewModel = colorDetailsViewModelFactory.create(
            coroutineScope = ViewModelCoroutineScope(parent = colorCenterViewModelCoroutineScope),
            colorDetailsEventStore = colorDetailsEventStore,
        )
        val colorSchemeViewModelCoroutineScope =
            ViewModelCoroutineScope(parent = colorCenterViewModelCoroutineScope)
        val colorSchemeEventStore = colorSchemeEventStoreProvider.get()
        val colorSchemeViewModel = colorSchemeViewModelFactory.create(
            coroutineScope = colorSchemeViewModelCoroutineScope,
            colorSchemeEventStore = colorSchemeEventStore,
        )
        val colorCenterViewModel = colorCenterViewModelFactory.create(
            coroutineScope = colorCenterViewModelCoroutineScope,
            colorDetailsViewModel = colorDetailsViewModel,
            colorSchemeViewModel = colorSchemeViewModel,
        )
        val selectedSwatchColorDetailsEventStore = colorDetailsEventStoreProvider.get()
        val selectedSwatchColorDetailsViewModel = colorDetailsViewModelFactory.create(
            coroutineScope = ViewModelCoroutineScope(parent = colorSchemeViewModelCoroutineScope),
            colorDetailsEventStore = selectedSwatchColorDetailsEventStore,
        )
        return ColorCenterComponents(
            colorCenterViewModel = colorCenterViewModel,
            colorCenterCoroutineScope = colorCenterViewModelCoroutineScope,
            colorDetailsEventStore = colorDetailsEventStore,
            colorSchemeEventStore = colorSchemeEventStore,
            selectedSwatchColorDetailsViewModel = selectedSwatchColorDetailsViewModel,
            selectedSwatchColorDetailsEventStore = selectedSwatchColorDetailsEventStore,
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
    val colorCenterCoroutineScope: CoroutineScope,
    val colorDetailsEventStore: ColorDetailsEventStore,
    val colorSchemeEventStore: ColorSchemeEventStore,
    val selectedSwatchColorDetailsViewModel: ColorDetailsViewModel,
    val selectedSwatchColorDetailsEventStore: ColorDetailsEventStore,
)