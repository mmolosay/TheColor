package io.github.mmolosay.thecolor.presentation.home.viewmodel

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.presentation.center.ColorCenterViewModel
import io.github.mmolosay.thecolor.presentation.common.viewmodel.ViewModelCoroutineScope
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsViewModel
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeViewModel
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
    private val colorDetailsViewModelFactory: ColorDetailsViewModel.Factory,
    private val colorSchemeViewModelFactory: ColorSchemeViewModel.Factory,
    private val colorCenterViewModelFactory: ColorCenterViewModel.Factory,
) {

    fun create(viewModelScope: CoroutineScope): ColorCenterComponents {
        val colorCenterViewModelCoroutineScope = ViewModelCoroutineScope(parent = viewModelScope)
        val colorDetailsViewModel = colorDetailsViewModelFactory.create(
            coroutineScope = ViewModelCoroutineScope(parent = colorCenterViewModelCoroutineScope),
        )
        val colorSchemeViewModelCoroutineScope =
            ViewModelCoroutineScope(parent = colorCenterViewModelCoroutineScope)
        val colorSchemeViewModel = colorSchemeViewModelFactory.create(
            coroutineScope = colorSchemeViewModelCoroutineScope,
        )
        val colorCenterViewModel = colorCenterViewModelFactory.create(
            coroutineScope = colorCenterViewModelCoroutineScope,
            colorDetailsViewModel = colorDetailsViewModel,
            colorSchemeViewModel = colorSchemeViewModel,
        )
        val selectedSwatchColorDetailsViewModel = colorDetailsViewModelFactory.create(
            coroutineScope = ViewModelCoroutineScope(parent = colorSchemeViewModelCoroutineScope),
        )
        return ColorCenterComponents(
            colorCenterViewModel = colorCenterViewModel,
            colorCenterCoroutineScope = colorCenterViewModelCoroutineScope,
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
    val colorCenterCoroutineScope: CoroutineScope,
    val selectedSwatchColorDetailsViewModel: ColorDetailsViewModel,
)