package io.github.mmolosay.thecolor.presentation.preview

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform

internal class ColorPreviewDataController(
    coroutineScope: CoroutineScope,
    actualDataFlow: StateFlow<ColorPreviewData>,
    uiStateControllerProxy: ColorPreviewUiStateControllerProxy,
) {
    private var isOnHold = false
    val controlledDataFlow: StateFlow<ColorPreviewData> =
        actualDataFlow
            .transform { actualData ->
                if (!isOnHold) emit(actualData)
            }
            .stateIn(
                scope = coroutineScope,
                started = SharingStarted.Companion.Eagerly,
                initialValue = actualDataFlow.value,
            )

    init {
        uiStateControllerProxy.delegate = UiStateControllerImpl()
    }

    private inner class UiStateControllerImpl : ColorPreviewUiStateController {

        override fun hold() {
            isOnHold = true
        }

        override fun release() {
            isOnHold = false
        }
    }
}