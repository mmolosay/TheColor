package io.github.mmolosay.thecolor.presentation.home.viewmodel

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.presentation.api.ViewModelCoroutineScope
import io.github.mmolosay.thecolor.presentation.center.ColorCenterViewModel
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsCommandStore
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsEventStore
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsViewModel
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeCommandStore
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeEventStore
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Provider

/**
 * Stores [ColorCenterComponents] in a [componentsFlow].
 * Provides methods for disposing of a current components when they are no longer needed
 * and for creating new components.
 */
/* private for HomeViewModel */
class ColorCenterComponentsStore @AssistedInject constructor(
    @Assisted private val viewModelScope: CoroutineScope,

    private val colorDetailsCommandStoreProvider: Provider<ColorDetailsCommandStore>,
    private val colorDetailsEventStoreProvider: Provider<ColorDetailsEventStore>,
    private val colorDetailsViewModelFactory: ColorDetailsViewModel.Factory,

    private val colorSchemeCommandStoreProvider: Provider<ColorSchemeCommandStore>,
    private val colorSchemeEventStoreProvider: Provider<ColorSchemeEventStore>,
    private val colorSchemeViewModelFactory: ColorSchemeViewModel.Factory,

    private val colorCenterViewModelFactory: ColorCenterViewModel.Factory,
) {

    private val _componentsFlow = MutableStateFlow<ColorCenterComponents?>(null)
    val componentsFlow = _componentsFlow.asStateFlow()

    @Synchronized
    fun createNewComponents() {
        disposeComponents() // dispose of current components if there are any
        val components = ColorCenterComponents()
        _componentsFlow.value = components
    }

    // TODO: in most cases, dispose will be called first from HomeViewModel. Then create will be called.
    //  however, in 1 in ~30 cases the order will be reversed. First, components will be created due to
    //  color session start; then, they will be incorrectly disposed of due to color session end.
    //  Implement a mechanism that prioritises dispose over create and executes it (dispose) first ALWAYS.
    //  Enable "resume from last searched color" feature in app settings.
    @Synchronized
    fun disposeComponents() {
        val components = components ?: return
        components.colorCenterViewModel.dispose() // will also dispose of its child ViewModels
        _componentsFlow.value = null
    }

    private fun ColorCenterComponents(): ColorCenterComponents {
        val colorCenterViewModelCoroutineScope = ViewModelCoroutineScope(parent = viewModelScope)
        val colorDetailsCommandStore = colorDetailsCommandStoreProvider.get()
        val colorDetailsEventStore = colorDetailsEventStoreProvider.get()
        val colorDetailsViewModel = colorDetailsViewModelFactory.create(
            coroutineScope = ViewModelCoroutineScope(parent = colorCenterViewModelCoroutineScope),
            colorDetailsCommandProvider = colorDetailsCommandStore,
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
            colorDetailsCommandProvider = selectedSwatchColorDetailsCommandStore,
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

    @AssistedFactory
    fun interface Factory {
        fun create(
            viewModelScope: CoroutineScope,
        ): ColorCenterComponentsStore
    }
}

val ColorCenterComponentsStore.components: ColorCenterComponents?
    get() = this.componentsFlow.value

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