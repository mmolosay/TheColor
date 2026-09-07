package io.github.mmolosay.thecolor.presentation.center

import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsHandle
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeHandle
import kotlinx.coroutines.flow.StateFlow

interface ColorCenterHandle {
    val dataFlow: StateFlow<ColorCenterData>
    val colorDetails: ColorDetailsHandle
    val colorScheme: ColorSchemeHandle
    fun facade(data: ColorCenterData): ColorCenterFacade
}

data class ColorCenterFacade(
    val data: ColorCenterData,
    val execute: ExecuteColorCenterAction,
)

fun ColorCenterHandle(viewModel: ColorCenterViewModel): ColorCenterHandle =
    ColorCenterHandleImpl(
        dataFlow = viewModel.dataFlow,
        colorDetails = ColorDetailsHandle(viewModel.colorDetailsViewModel),
        colorScheme = ColorSchemeHandle(viewModel.colorSchemeViewModel),
        execute = viewModel::execute,
    )

private class ColorCenterHandleImpl(
    override val dataFlow: StateFlow<ColorCenterData>,
    override val colorDetails: ColorDetailsHandle,
    override val colorScheme: ColorSchemeHandle,
    private val execute: ExecuteColorCenterAction,
) : ColorCenterHandle {

    override fun facade(data: ColorCenterData): ColorCenterFacade =
        ColorCenterFacade(
            data = data,
            execute = execute,
        )
}