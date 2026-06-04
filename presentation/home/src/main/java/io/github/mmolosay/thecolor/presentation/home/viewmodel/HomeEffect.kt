package io.github.mmolosay.thecolor.presentation.home.viewmodel

/**
 * One-time notification that originates in the `Home` feature and is broadcast to the UI.
 *
 * May take any form, e.g. be an event or a call to action.
 */
sealed interface HomeEffect {

    /**
     * Requests UI to open / navigate to / focus 'Settings' feature.
     */
    data object GoToSettings : HomeEffect
}