package io.github.mmolosay.thecolor.presentation.api.nav.bar

fun interface NavBarAppearanceControllerTree {

    /**
     * Creates a child [NavBarAppearanceController] that can be used by a component to safely modify
     * the appearance stack.
     * For example, once component is removed, and it wants to remove all appearances it has [push]ed,
     * it may do so using [NavBarAppearanceStack.clear].
     * When the parent controller is cleared, so is the child one.
     */
    fun branch(tag: Any): NavBarAppearanceController
}