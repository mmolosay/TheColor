package io.github.mmolosay.thecolor.presentation.input.impl.field

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.model.UserPreferences
import io.github.mmolosay.thecolor.domain.repository.DefaultUserPreferences
import io.github.mmolosay.thecolor.domain.repository.UserPreferencesRepository
import io.github.mmolosay.thecolor.presentation.common.viewmodel.SimpleViewModel
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldData.ClearTextFeature
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldData.Text
import io.github.mmolosay.thecolor.presentation.input.impl.model.Update
import io.github.mmolosay.thecolor.presentation.input.impl.model.causedByUser
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
    @Assisted initialText: String,
    @Assisted coroutineScope: CoroutineScope,
    @Assisted private val filterUserInput: (String) -> Text,
    @Assisted private val enableClearTextFeature: Boolean,
    private val userPreferencesRepository: UserPreferencesRepository,
    @Named("defaultDispatcher") private val defaultDispatcher: CoroutineDispatcher,
    @Named("uiDataUpdateDispatcher") private val uiDataUpdateDispatcher: CoroutineDispatcher,
) : SimpleViewModel(coroutineScope) {

    private val dataUpdateMutex = Mutex()

    private val _dataUpdatesFlow: MutableStateFlow<Update<TextFieldData>> = kotlin.run {
        val data = makeInitialData(Text(initialText))
        val update = data causedByUser false // coerce initial Update to be caused by not a user
        MutableStateFlow(update)
    }
    val dataUpdatesFlow = _dataUpdatesFlow.asStateFlow()

    init {
        collectSelectAllTextOnTextFieldFocusPreference()
    }

    private fun collectSelectAllTextOnTextFieldFocusPreference() {
        fun updateData(preference: UserPreferences.SelectAllTextOnTextFieldFocus) {
            _dataUpdatesFlow.update { update ->
                val newData = update.payload.copy(shouldSelectAllTextOnFocus = preference.enabled)
                Update(payload = newData, causedByUser = update.causedByUser)
            }
        }
        coroutineScope.launch(defaultDispatcher) {
            userPreferencesRepository.flowOfSelectAllTextOnTextFieldFocus
                .filterNotNull()
                .collect(::updateData)
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
                        val oldData = it.payload
                        val newData = oldData.smartCopy(text)
                        Update(payload = newData, causedByUser = update.causedByUser)
                    }
                }
            }
        }
    }

    private fun TextFieldData.smartCopy(text: Text) =
        this.copy(
            text = text,
            clearText = clearTextFeatureOrNull(text),
        )

    private fun clearTextFeatureOrNull(text: Text): ClearTextFeature? {
        if (!enableClearTextFeature) return null
        return object : ClearTextFeature {
            override val willBeIdempotent: Boolean =
                text.string.isEmpty()
            override fun invoke() =
                updateTextByUser(Text(""))
        }
    }

    private fun makeInitialData(text: Text) =
        TextFieldData(
            text = text,
            onTextChange = ::updateTextByUser, // the client of this ViewModel is a View, all text changes come from View (user)
            filterUserInput = filterUserInput,
            clearText = clearTextFeatureOrNull(text),
            shouldSelectAllTextOnFocus = userPreferencesRepository
                .flowOfSelectAllTextOnTextFieldFocus
                .value.let { it ?: DefaultUserPreferences.SelectAllTextOnTextFieldFocus }
                .enabled,
        )

    @AssistedFactory
    interface Factory {

        /**
         * @param initialText declared type is a [String], but it is intended to be a [Text].
         * At the moment, Dagger doesn't deal with name mangling correctly, which occurs due to
         * the [Text] being a `value class`.
         * https://github.com/google/dagger/issues/4613
         * https://kotlinlang.org/docs/inline-classes.html#mangling
         * Taking this into account, the `ViewModel` will treat the [initialText] as [Text] and
         * won't process it in any way, as it would've done for a raw user input [String].
         * See [TextFieldData.filterUserInput].
         */
        fun create(
            initialText: String = "",
            coroutineScope: CoroutineScope,
            filterUserInput: (String) -> Text,
            enableClearTextFeature: Boolean,
        ): TextFieldViewModel
    }
}

/**
 * Update text when it comes not from UI or user input.
 */
internal infix fun TextFieldViewModel.updateText(text: Text) =
    updateText(text causedByUser false)

private fun TextFieldViewModel.updateTextByUser(text: Text) =
    updateText(text causedByUser true)