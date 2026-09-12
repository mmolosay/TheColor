package io.github.mmolosay.thecolor.presentation.input.group

import kotlinx.coroutines.Job
import io.github.mmolosay.thecolor.domain.color.ColorInputType as DomainColorInputType

sealed interface ColorInputGroupAction {

    data class ChangeInputType(
        val type: DomainColorInputType,
    ) : ColorInputGroupAction
}

typealias ExecuteColorInputGroupAction = (ColorInputGroupAction) -> Job