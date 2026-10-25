package io.github.mmolosay.thecolor.presentation.center

import kotlinx.coroutines.flow.StateFlow

interface ColorCenterHandle {
    val dataFlow: StateFlow<ColorCenterData>
    fun facade(data: ColorCenterData): ColorCenterFacade
}

data class ColorCenterFacade(
    val data: ColorCenterData,
    val execute: ExecuteColorCenterAction,
)

fun ColorCenterHandle(viewModel: ColorCenterViewModel): ColorCenterHandle =
    ColorCenterHandleImpl(
        dataFlow = viewModel.dataFlow,
        execute = viewModel::execute,
    )

private class ColorCenterHandleImpl(
    override val dataFlow: StateFlow<ColorCenterData>,
    private val execute: ExecuteColorCenterAction,
) : ColorCenterHandle {

    override fun facade(data: ColorCenterData): ColorCenterFacade =
        ColorCenterFacade(
            data = data,
            execute = execute,
        )
}