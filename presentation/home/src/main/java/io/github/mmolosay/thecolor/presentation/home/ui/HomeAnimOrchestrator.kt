package io.github.mmolosay.thecolor.presentation.home.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow

internal class HomeAnimOrchestrator(
    initialState: HomeAnimState,
) {
    val flowOfAnimState = MutableStateFlow<HomeAnimState>(initialState)
    var flowOfAnimDest = MutableStateFlow<HomeAnimState>(initialState)

    fun animDestReached(dest: HomeAnimState.ColorPreview) {

    }
}

internal val HomeAnimOrchestrator.animState
    @Composable
    get() = this.flowOfAnimState.collectAsStateWithLifecycle().value

internal val HomeAnimOrchestrator.animDest
    @Composable
    get() = this.flowOfAnimDest.collectAsStateWithLifecycle().value