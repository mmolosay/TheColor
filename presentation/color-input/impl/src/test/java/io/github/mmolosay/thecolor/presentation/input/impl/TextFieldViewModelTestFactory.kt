package io.github.mmolosay.thecolor.presentation.input.impl

import io.github.mmolosay.thecolor.domain.repository.UserPreferencesRepository
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldData
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope

/**
 * An implementation of [TextFieldViewModel.Factory] that employs external parameters passed in
 * [create] and internal parameters passed to a constructor to create a real [TextFieldViewModel].
 */
class TextFieldViewModelTestFactory(
    val userPreferencesRepository: UserPreferencesRepository,
    val defaultDispatcher: CoroutineDispatcher,
    val uiDataUpdateDispatcher: CoroutineDispatcher,
) : TextFieldViewModel.Factory {

    override fun create(
        coroutineScope: CoroutineScope,
        filterUserInput: (String) -> TextFieldData.Text,
    ): TextFieldViewModel {
        return TextFieldViewModel(
            coroutineScope = coroutineScope,
            filterUserInput = filterUserInput,
            userPreferencesRepository = userPreferencesRepository,
            defaultDispatcher = defaultDispatcher,
            uiDataUpdateDispatcher = uiDataUpdateDispatcher,
        )
    }
}