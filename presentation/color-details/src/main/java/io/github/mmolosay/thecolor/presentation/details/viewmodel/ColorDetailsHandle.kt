package io.github.mmolosay.thecolor.presentation.details.viewmodel

interface ColorDetailsHandle {
    fun facade(state: ColorDetailsState): ColorDetailsFacade
}

/**
 * A [ColorDetailsState] paired with the means to act on it.
 *
 * Every property must be derived from [state]: facades with equal states must themselves be equal.
 * Consumers rely on this to tell when nothing has changed, so a property that varies independently
 * of [state] would make equal states look different.
 */
data class ColorDetailsFacade(
    val state: ColorDetailsState,
    val execute: ExecuteColorDetailsAction,
)

fun ColorDetailsHandle(viewModel: ColorDetailsViewModel): ColorDetailsHandle =
    ColorDetailsHandleImpl(
        execute = viewModel::execute,
    )

private class ColorDetailsHandleImpl(
    private val execute: ExecuteColorDetailsAction,
) : ColorDetailsHandle {

    override fun facade(state: ColorDetailsState): ColorDetailsFacade =
        ColorDetailsFacade(
            state = state,
            execute = execute,
        )
}