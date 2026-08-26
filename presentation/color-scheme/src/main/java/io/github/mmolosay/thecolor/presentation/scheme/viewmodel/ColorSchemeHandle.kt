package io.github.mmolosay.thecolor.presentation.scheme.viewmodel

interface ColorSchemeHandle {
    fun facade(state: ColorSchemeState): ColorSchemeFacade
}

data class ColorSchemeFacade(
    val state: ColorSchemeState,
    val execute: ExecuteColorSchemeAction,
)

fun ColorSchemeHandle(viewModel: ColorSchemeViewModel): ColorSchemeHandle =
    ColorSchemeHandleImpl(
        execute = viewModel::execute,
    )

private class ColorSchemeHandleImpl(
    private val execute: ExecuteColorSchemeAction,
) : ColorSchemeHandle {

    override fun facade(state: ColorSchemeState): ColorSchemeFacade =
        ColorSchemeFacade(
            state = state,
            execute = execute,
        )
}