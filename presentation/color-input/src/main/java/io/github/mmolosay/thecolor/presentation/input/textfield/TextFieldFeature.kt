package io.github.mmolosay.thecolor.presentation.input.textfield

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.domain.user.preferences.DefaultUserPreferences
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferencesRepository
import io.github.mmolosay.thecolor.domain.utils.readyValues
import io.github.mmolosay.thecolor.utils.mapState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TextFieldFeature @AssistedInject constructor(
    @Assisted private val core: TextFieldCore,
    @Assisted private val store: Store<TextFieldState, *>,
    @Assisted private val scope: CoroutineScope,
    private val userPreferencesRepository: UserPreferencesRepository,
) {

    val viewData: StateFlow<TextFieldViewData> =
        store.mapState { state -> with(core) { state.toViewData() } }

    init {
        collectSelectAllTextOnFocus()
    }

    private fun collectSelectAllTextOnFocus() {
        scope.launch {
            userPreferencesRepository.flowOfSelectAllTextOnTextFieldFocus
                .readyValues(DefaultUserPreferences.SelectAllTextOnTextFieldFocus)
                .collect { preference ->
                    store.update { state ->
                        with(core) { state.applySelectAllTextOnFocus(preference.enabled) }
                    }
                }
        }
    }

    fun clearText() {
        store.update { state ->
            with(core) { state.clearText() }
        }
    }

    fun enterText(text: String, expected: String) {
        store.update { state ->
            with(core) { state.enterText(text, expected) }
        }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            core: TextFieldCore,
            store: Store<TextFieldState, *>,
            scope: CoroutineScope,
        ): TextFieldFeature
    }
}