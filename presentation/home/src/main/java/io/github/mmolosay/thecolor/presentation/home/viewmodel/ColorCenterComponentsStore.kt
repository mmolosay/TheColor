package io.github.mmolosay.thecolor.presentation.home.viewmodel

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.presentation.center.ColorCenterViewModel
import io.github.mmolosay.thecolor.presentation.common.viewmodel.ViewModelCoroutineScope
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsCommandStore
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsEventStore
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsViewModel
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeCommandStore
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
    private val colorDetailsCommandStoreProvider: Provider<ColorDetailsCommandStore>,
    private val colorDetailsEventStoreProvider: Provider<ColorDetailsEventStore>,
    private val colorDetailsViewModelFactory: ColorDetailsViewModel.Factory,

    private val colorSchemeCommandStoreProvider: Provider<ColorSchemeCommandStore>,
    private val colorSchemeEventStoreProvider: Provider<ColorSchemeEventStore>,
    private val colorSchemeViewModelFactory: ColorSchemeViewModel.Factory,

    private val colorCenterViewModelFactory: ColorCenterViewModel.Factory,
) {

    fun create(viewModelScope: CoroutineScope): ColorCenterComponents {
        val colorCenterViewModelCoroutineScope = ViewModelCoroutineScope(parent = viewModelScope)
        val colorDetailsCommandStore = colorDetailsCommandStoreProvider.get()
        val colorDetailsEventStore = colorDetailsEventStoreProvider.get()
        val colorDetailsViewModel = colorDetailsViewModelFactory.create(
            coroutineScope = ViewModelCoroutineScope(parent = colorCenterViewModelCoroutineScope),
            colorDetailsCommandStore = colorDetailsCommandStore,
            colorDetailsEventStore = colorDetailsEventStore,
        )
        val colorSchemeViewModelCoroutineScope =
            ViewModelCoroutineScope(parent = colorCenterViewModelCoroutineScope)
        val colorSchemeCommandStore = colorSchemeCommandStoreProvider.get()
        val colorSchemeEventStore = colorSchemeEventStoreProvider.get()
        val colorSchemeViewModel = colorSchemeViewModelFactory.create(
            coroutineScope = colorSchemeViewModelCoroutineScope,
            colorSchemeCommandProvider = colorSchemeCommandStore,
            colorSchemeEventStore = colorSchemeEventStore,
        )
        val colorCenterViewModel = colorCenterViewModelFactory.create(
            coroutineScope = colorCenterViewModelCoroutineScope,
            colorDetailsViewModel = colorDetailsViewModel,
            colorSchemeViewModel = colorSchemeViewModel,
        )
        val selectedSwatchColorDetailsCommandStore = colorDetailsCommandStoreProvider.get()
        val selectedSwatchColorDetailsEventStore = colorDetailsEventStoreProvider.get()
        val selectedSwatchColorDetailsViewModel = colorDetailsViewModelFactory.create(
            coroutineScope = ViewModelCoroutineScope(parent = colorSchemeViewModelCoroutineScope),
            colorDetailsCommandStore = selectedSwatchColorDetailsCommandStore,
            colorDetailsEventStore = selectedSwatchColorDetailsEventStore,
        )
        return ColorCenterComponents(
            colorCenterViewModel = colorCenterViewModel,
            colorCenterCoroutineScope = colorCenterViewModelCoroutineScope,
            colorDetailsCommandStore = colorDetailsCommandStore,
            colorDetailsEventStore = colorDetailsEventStore,
            colorSchemeCommandStore = colorSchemeCommandStore,
            colorSchemeEventStore = colorSchemeEventStore,
            selectedSwatchColorDetailsViewModel = selectedSwatchColorDetailsViewModel,
            selectedSwatchColorDetailsCommandStore = selectedSwatchColorDetailsCommandStore,
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
    val colorDetailsCommandStore: ColorDetailsCommandStore,
    val colorDetailsEventStore: ColorDetailsEventStore,
    val colorSchemeCommandStore: ColorSchemeCommandStore,
    val colorSchemeEventStore: ColorSchemeEventStore,
    val selectedSwatchColorDetailsViewModel: ColorDetailsViewModel,
    val selectedSwatchColorDetailsCommandStore: ColorDetailsCommandStore,
    val selectedSwatchColorDetailsEventStore: ColorDetailsEventStore,
)