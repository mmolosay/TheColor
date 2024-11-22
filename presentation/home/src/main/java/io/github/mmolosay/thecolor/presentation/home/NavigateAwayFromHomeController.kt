package io.github.mmolosay.thecolor.presentation.home

/**
 * A callback that is invoked when navigating away from Home.
 */
fun interface OnNavigateAwayFromHomeListener {
    fun onNavigateAwayFromHome()
}

interface NavigateAwayFromHomeController {
    fun add(listener: OnNavigateAwayFromHomeListener)
    fun remove(listener: OnNavigateAwayFromHomeListener)
}

object NoopNavigateAwayFromHomeController : NavigateAwayFromHomeController {
    override fun add(listener: OnNavigateAwayFromHomeListener) {}
    override fun remove(listener: OnNavigateAwayFromHomeListener) {}
}