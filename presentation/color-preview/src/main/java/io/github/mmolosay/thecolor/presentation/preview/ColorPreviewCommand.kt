package io.github.mmolosay.thecolor.presentation.preview

import io.github.mmolosay.thecolor.domain.color.Color
import kotlinx.coroutines.CompletableDeferred

/**
 * A command issued towards 'Color Preview' feature to be handled by it.
 *
 * Processed in [ColorPreviewViewModel]. Should only be used to communicate with
 * 'Color Preview' feature from other features and `ViewModel`s.
 */
sealed interface ColorPreviewCommand {

    /**
     * Sets the new [color]. It will be transformed to the [ColorPreviewData] and exposed via [dataFlow].
     *
     * The [completion] will be marked as [complete][CompletableDeferred.complete] once the processing of the new
     * [color] is finished and the [dataFlow][ColorPreviewViewModel.dataFlow] has emitted a new data.
     *
     * All [SetColor] commands are conflated, meaning that if there is a command being processed
     * at the moment, then a new incoming command will cancel the ongoing one.
     */
    data class SetColor(
        val color: Color?,
        val completion: CompletableDeferred<Unit> = CompletableDeferred(),
    ) : ColorPreviewCommand
}