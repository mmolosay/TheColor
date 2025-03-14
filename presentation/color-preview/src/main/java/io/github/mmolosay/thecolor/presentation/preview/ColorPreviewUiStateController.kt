package io.github.mmolosay.thecolor.presentation.preview

/**
 * Allows to manipulate current [ColorPreviewUiState].
 */
sealed interface ColorPreviewUiStateController {
    fun hold()
    fun release()
}

/**
 * An implementation that simply delegates calls to wrapped implementation.
 */
class ColorPreviewUiStateControllerProxy : ColorPreviewUiStateController {

    // 'lateinit' is used with purpose to throw error when delegate is not set
    lateinit var delegate: ColorPreviewUiStateController

    override fun hold() {
        delegate.hold()
    }

    override fun release() {
        delegate.release()
    }
}

object NoopColorPreviewUiStateController : ColorPreviewUiStateController {
    override fun hold() {}
    override fun release() {}
}