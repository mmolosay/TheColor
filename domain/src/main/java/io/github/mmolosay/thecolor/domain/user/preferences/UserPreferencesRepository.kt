package io.github.mmolosay.thecolor.domain.user.preferences

import io.github.mmolosay.thecolor.domain.color.ColorInputType
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.AutoProceedWithRandomizedColors
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.DynamicUiColors
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.ResumeFromLastSearchedColorOnStartup
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.SelectAllTextOnTextFieldFocus
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.SmartBackspace
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.UiColorSchemeSet
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferencesRepository.DataState
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferencesRepository.IllegalStoredValueException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

interface UserPreferencesRepository {
    val flowOfColorInputType: StateFlow<DataState<ColorInputType>>
    suspend fun setColorInputType(value: ColorInputType?)

    val flowOfAppUiColorSchemeSet: StateFlow<DataState<UiColorSchemeSet>>
    suspend fun setAppUiColorSchemeSet(value: UiColorSchemeSet?)

    val flowOfDynamicUiColors: StateFlow<DataState<DynamicUiColors>>
    suspend fun setDynamicUiColors(value: DynamicUiColors?)

    val flowOfResumeFromLastSearchedColorOnStartup: StateFlow<DataState<ResumeFromLastSearchedColorOnStartup>>
    suspend fun setResumeFromLastSearchedColorOnStartup(value: ResumeFromLastSearchedColorOnStartup?)

    val flowOfSmartBackspace: StateFlow<DataState<SmartBackspace>>
    suspend fun setSmartBackspace(value: SmartBackspace?)

    val flowOfSelectAllTextOnTextFieldFocus: StateFlow<DataState<SelectAllTextOnTextFieldFocus>>
    suspend fun setSelectAllTextOnTextFieldFocus(value: SelectAllTextOnTextFieldFocus?)

    val flowOfAutoProceedWithRandomizedColors: StateFlow<DataState<AutoProceedWithRandomizedColors>>
    suspend fun setAutoProceedWithRandomizedColors(value: AutoProceedWithRandomizedColors?)

    sealed interface DataState<out T> {
        data object BeingInitialized : DataState<Nothing>
        data object NoValueStored : DataState<Nothing>
        data class HasValueStored<T>(val value: T) : DataState<T>
    }

    class IllegalStoredValueException(propertyName: String, value: Any?) : IllegalStateException(
        "Property '$propertyName' has unsupported stored value: $value"
    )
}

@OptIn(ExperimentalContracts::class)
inline fun <T> DataState<T>.valueOrElse(
    block: () -> T,
): T {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    return when (this) {
        is DataState.BeingInitialized -> block()
        is DataState.NoValueStored -> block()
        is DataState.HasValueStored -> this.value
    }
}

fun <T> Flow<DataState<T>>.filterOutBeingInitialized(): Flow<DataState<T>> =
    this.filter { it !is DataState.BeingInitialized }

// syntactic sugar
inline fun <reified T> IllegalStoredValueException(value: Any?) =
    IllegalStoredValueException(
        propertyName = T::class.simpleName!!,
        value = value,
    )