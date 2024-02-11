package io.github.mmolosay.thecolor.presentation.scheme

import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeData.Actions
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeData.Models

object ColorSchemeDataFactory {

    fun create(
        models: Models,
        actions: Actions,
    ) =
        ColorSchemeData(
            swatches = models.swatches,
            activeMode = models.activeMode,
            selectedMode = models.selectedMode,
            onModeSelect = actions.onModeSelect,
            activeSwatchCount = models.activeSwatchCount,
            selectedSwatchCount = models.selectedSwatchCount,
            onSwatchCountSelect = actions.onSwatchCountSelect,
            changes = Changes(models, actions),
        )

    private fun Changes(
        models: Models,
        actions: Actions,
    ) =
        if (models.hasChanges)
            ColorSchemeData.Changes.Present(applyChanges = actions.applyChanges)
        else
            ColorSchemeData.Changes.None
}