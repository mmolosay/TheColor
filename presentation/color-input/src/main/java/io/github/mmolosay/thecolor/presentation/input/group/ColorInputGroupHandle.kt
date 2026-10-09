package io.github.mmolosay.thecolor.presentation.input.group

import kotlinx.coroutines.flow.StateFlow

interface ColorInputGroupHandle {
    val dataFlow: StateFlow<ColorInputGroupData>
    fun facade(data: ColorInputGroupData): ColorInputGroupFacade
}

data class ColorInputGroupFacade(
    val data: ColorInputGroupData,
    val execute: ExecuteColorInputGroupAction,
)

fun ColorInputGroupHandle(viewModel: ColorInputGroupViewModel): ColorInputGroupHandle =
    ColorInputGroupHandleImpl(
        dataFlow = viewModel.dataFlow,
        execute = viewModel::execute,
    )

private class ColorInputGroupHandleImpl(
    override val dataFlow: StateFlow<ColorInputGroupData>,
    private val execute: ExecuteColorInputGroupAction,
) : ColorInputGroupHandle {

    override fun facade(data: ColorInputGroupData): ColorInputGroupFacade =
        ColorInputGroupFacade(
            data = data,
            execute = execute,
        )
}