package io.github.mmolosay.thecolor.presentation.input.impl.model

import io.github.mmolosay.thecolor.presentation.input.api.ColorInput
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputState

/**
 * Couples a [coreData] data which is exposed from ViewModel with various values that are
 * produced from this [coreData] data.
 * This is a convenience class that keeps close "source" data and cached values obtained from it.
 */
internal data class FullData<CoreData, ColorInputSpace : ColorInput>(
    val coreData: CoreData,
    val colorInput: ColorInputSpace,
    val colorInputState: ColorInputState,
)