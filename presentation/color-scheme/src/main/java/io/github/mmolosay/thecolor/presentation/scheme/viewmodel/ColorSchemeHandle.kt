package io.github.mmolosay.thecolor.presentation.scheme.viewmodel

import kotlinx.coroutines.flow.StateFlow

interface ColorSchemeHandle {
    val stateFlow: StateFlow<ColorSchemeState>
    fun facade(state: ColorSchemeState): ColorSchemeFacade
}

data class ColorSchemeFacade(
    val state: ColorSchemeState,
    val execute: ExecuteColorSchemeAction,
)

fun ColorSchemeHandle(
    stateFlow: StateFlow<ColorSchemeState>,
    execute: ExecuteColorSchemeAction,
): ColorSchemeHandle =
    ColorSchemeHandleImpl(
        stateFlow = stateFlow,
        execute = execute,
    )

private class ColorSchemeHandleImpl(
    override val stateFlow: StateFlow<ColorSchemeState>,
    private val execute: ExecuteColorSchemeAction,
) : ColorSchemeHandle {

    override fun facade(state: ColorSchemeState): ColorSchemeFacade =
        ColorSchemeFacade(
            state = state,
            execute = execute,
        )
}