package io.github.mmolosay.thecolor.presentation.input.group

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.user.preferences.DefaultUserPreferences
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferencesRepository
import io.github.mmolosay.thecolor.domain.utils.readyValues
import io.github.mmolosay.thecolor.utils.composition.Store
import io.github.mmolosay.thecolor.utils.mapState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import io.github.mmolosay.thecolor.domain.color.ColorInputType as DomainColorInputType

class ColorInputGroupFeature @AssistedInject constructor(
    @Assisted private val core: ColorInputGroupCore,
    @Assisted private val store: Store<ColorInputGroupState, *>,
    @Assisted private val scope: CoroutineScope,
    private val userPreferencesRepository: UserPreferencesRepository,
) {

    val viewData: StateFlow<ColorInputGroupViewData?> =
        store.mapState { state -> with(core) { state.toViewData() } }

    init {
        initPreferredType()
    }

    private fun initPreferredType() {
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            val type = userPreferencesRepository.flowOfColorInputType
                .readyValues(DefaultUserPreferences.PreferredColorInputType)
                .first()
            store.update { state ->
                with(core) { state.applyPreferredType(type) }
            }
        }
    }

    fun select(type: DomainColorInputType) {
        store.update { state ->
            with(core) { state.select(type) }
        }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            core: ColorInputGroupCore,
            store: Store<ColorInputGroupState, *>,
            scope: CoroutineScope,
        ): ColorInputGroupFeature
    }
}