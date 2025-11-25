package io.github.mmolosay.thecolor.presentation.input.impl.field

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.repository.DefaultUserPreferences
import io.github.mmolosay.thecolor.domain.repository.UserPreferencesRepository
import io.github.mmolosay.thecolor.presentation.common.viewmodel.SimpleViewModel
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldData.Text
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldData.TrailingButton
import io.github.mmolosay.thecolor.presentation.input.impl.model.Update
import io.github.mmolosay.thecolor.presentation.input.impl.model.causedByUser
import io.github.mmolosay.thecolor.presentation.input.impl.model.map
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Named

/**
 * Handles presentation logic of a single text field inside a 'Color Input' feature.
 *
 * Unlike typical `ViewModel`s, it doesn't derive from Google's [ViewModel][androidx.lifecycle.ViewModel],
 * thus cannot be instantiated using [ViewModelProvider][androidx.lifecycle.ViewModelProvider].
 *
 * Instead, it can be created within "simple" `ViewModel` or Google's `ViewModel`.
 */
internal class TextFieldViewModel @AssistedInject constructor(
    @Assisted coroutineScope: CoroutineScope,
    @Assisted private val filterUserInput: (String) -> Text,
    @Assisted private val allowTrailingButton: Boolean,
    private val userPreferencesRepository: UserPreferencesRepository,
    @Named("defaultDispatcher") private val defaultDispatcher: CoroutineDispatcher,
    @Named("uiDataUpdateDispatcher") private val uiDataUpdateDispatcher: CoroutineDispatcher,
) : SimpleViewModel(coroutineScope) {

    private val dataUpdateMutex = Mutex()
    private val _dataUpdatesFlow = MutableStateFlow<Update<TextFieldData>?>(null)
    val dataUpdatesFlow = _dataUpdatesFlow.asStateFlow()

    init {
        collectSelectAllTextOnTextFieldFocusPreference()
    }

    private fun collectSelectAllTextOnTextFieldFocusPreference() {
        coroutineScope.launch(defaultDispatcher) {
            userPreferencesRepository
                .flowOfSelectAllTextOnTextFieldFocus
                .filterNotNull()
                .collect { preference ->
                    _dataUpdatesFlow.update { update ->
                        if (update == null) return@update null
                        update.map {
                            it.copy(shouldSelectAllTextOnFocus = preference.enabled)
                        }
                    }
                }
        }
    }

    fun updateText(update: Update<Text>) {
        coroutineScope.launch(defaultDispatcher) {
            /*
             * MutableStateFlow.update() is NOT fair. If we:
             * 1. call update() that will set value to X
             * 2. call update() that will set value to Y
             * So may happen that the second update() finishes first, and flow will emit [Y, X]
             * instead of [X, Y], which is expected according to the order of calling update()s.
             * We need a mutex (which IS fair) to prevent other coroutines from entering update()
             * and thus potentially messing up the order of emissions.
             */
            dataUpdateMutex.withLock {
                withContext(uiDataUpdateDispatcher) {
                    _dataUpdatesFlow.update {
                        val text = update.payload
                        val newData = if (it == null) {
                            makeInitialData(text)
                        } else {
                            val oldData = it.payload
                            oldData.smartCopy(text)
                        }
                        newData causedByUser update.causedByUser
                    }
                }
            }
        }
    }

    private fun onTextChangeFromView(text: Text) =
        updateText(text causedByUser true)

    private fun TextFieldData.smartCopy(text: Text) =
        copy(
            text = text,
            trailingButton = trailingButton(text),
        )

    private fun trailingButton(text: Text): TrailingButton? {
        if (!allowTrailingButton) return null
        val showTrailingButton = text.string.isNotEmpty()
        return when (showTrailingButton) {
            true -> TrailingButton(onClick = { onTextChangeFromView(Text("")) })
            false -> null
        }
    }

    private fun makeInitialData(text: Text) =
        TextFieldData(
            text = text,
            onTextChange = ::onTextChangeFromView,
            filterUserInput = filterUserInput,
            trailingButton = trailingButton(text),
            shouldSelectAllTextOnFocus = userPreferencesRepository
                .flowOfSelectAllTextOnTextFieldFocus
                .value
                .let { it ?: DefaultUserPreferences.SelectAllTextOnTextFieldFocus }
                .enabled,
        )

    @AssistedFactory
    interface Factory {
        fun create(
            coroutineScope: CoroutineScope,
            filterUserInput: (String) -> Text,
            allowTrailingButton: Boolean,
        ): TextFieldViewModel
    }
}

/**
 * Update text when it comes not from UI or user input.
 */
internal infix fun TextFieldViewModel.updateText(text: Text) {
    val update = text causedByUser false
    this.updateText(update)
}