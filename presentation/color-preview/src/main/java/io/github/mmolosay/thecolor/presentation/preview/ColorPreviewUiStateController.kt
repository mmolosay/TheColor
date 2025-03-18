package io.github.mmolosay.thecolor.presentation.preview

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewUiState as UiState

/**
 * Allows to manipulate actual flow of [UiState].
 * In the essence, this class transforms [actualUiStateFlow] into processed [uiStateFlow].
 * You can think of it as an operator on the flow.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ColorPreviewUiStateController(
    coroutineScope: CoroutineScope,
    private val actualUiStateFlow: StateFlow<UiState>,
) {
    var filter: ColorPreviewUiStateFilter? = null
    val uiStateFlow: StateFlow<UiState>
    val manualUiStateFlow: MutableSharedFlow<UiState>

    init {
        val filtered = actualUiStateFlow.transformLatest { uiState ->
            val shouldEmit = kotlin.run {
                val filter = filter
                if (filter != null) filter.submit(uiState) else true
            }
            if (shouldEmit) emit(uiState)
        }
        val manual = MutableSharedFlow<UiState>(replay = 1)
            .also { manualUiStateFlow = it }
        uiStateFlow = merge(filtered, manual)
            .stateIn(coroutineScope, SharingStarted.Eagerly, actualUiStateFlow.value)
    }

    fun catchUp() {
        val actual = actualUiStateFlow.value
        val wasEmitted = manualUiStateFlow.tryEmit(actual)
        // atm I'm not sure whether tryEmit will work in 100% of cases. May require using suspending emit.
        if (!wasEmitted) error("manualUiStateFlow couldn't emit new value")
    }
}

fun ColorPreviewUiStateController(
    coroutineScope: CoroutineScope,
    dataFlow: StateFlow<ColorPreviewData>,
): ColorPreviewUiStateController {
    val uiStateFlow = dataFlow
        .map { data -> data.toUiState() }
        .stateIn(coroutineScope, SharingStarted.Eagerly, dataFlow.value.toUiState())
    return ColorPreviewUiStateController(
        coroutineScope = coroutineScope,
        actualUiStateFlow = uiStateFlow,
    )
}

/** Allows to filter incoming [ColorPreviewUiState]s and either accept or skip it. */
fun interface ColorPreviewUiStateFilter {

    /** Returns `true` is this [uiState] is accepted to be set to View, otherwise `false`. */
    suspend fun submit(uiState: UiState): Boolean
}