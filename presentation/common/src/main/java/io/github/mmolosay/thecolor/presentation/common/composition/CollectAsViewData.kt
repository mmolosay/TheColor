package io.github.mmolosay.thecolor.presentation.common.composition

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.thecolor.composition.ViewDataSource

// experiment: collects the view data while the screen is started, instead of listening to commits
@Composable
fun <V> ViewDataSource<V>.collectAsViewData(): State<V> =
    this.flow.collectAsStateWithLifecycle()
