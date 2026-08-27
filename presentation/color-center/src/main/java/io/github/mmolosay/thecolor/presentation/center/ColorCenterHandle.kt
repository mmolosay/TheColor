package io.github.mmolosay.thecolor.presentation.center

interface ColorCenterHandle {
    fun facade(data: ColorCenterData): ColorCenterFacade
}

data class ColorCenterFacade(
    val data: ColorCenterData,
    val execute: ExecuteColorCenterAction,
)

fun ColorCenterHandle(viewModel: ColorCenterViewModel): ColorCenterHandle =
    ColorCenterHandleImpl(
        execute = viewModel::execute,
    )

private class ColorCenterHandleImpl(
    private val execute: ExecuteColorCenterAction,
) : ColorCenterHandle {

    override fun facade(data: ColorCenterData): ColorCenterFacade =
        ColorCenterFacade(
            data = data,
            execute = execute,
        )
}