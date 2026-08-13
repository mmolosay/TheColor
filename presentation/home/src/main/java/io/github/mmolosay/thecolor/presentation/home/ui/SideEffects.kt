package io.github.mmolosay.thecolor.presentation.home.ui

import android.widget.Toast
import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeData.ProceedResult
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeData.SideEffect
import io.github.mmolosay.thecolor.utils.cache.DequeCache
import io.github.mmolosay.thecolor.utils.cache.PruneOnSizeThreshold
import io.github.mmolosay.thecolor.utils.doNothing

@Composable
internal fun ProcessSideEffectsAsSideEffect(
    sideEffects: List<SideEffect>,
    onSideEffectProcessed: (SideEffect) -> Unit,
    navigateToSettings: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    fun process(se: SideEffect.GoToSettings) {
        focusManager.clearFocus()
        navigateToSettings()
        onSideEffectProcessed(se)
    }
    for (se in sideEffects) {
        key(se.id) {
            LaunchedEffect(Unit) {
                when (se) {
                    is SideEffect.GoToSettings -> process(se)
                }
            }
        }
    }
}

@Composable
internal fun ProcessProceedResultAsSideEffect(
    proceedResult: ProceedResult?,
    strings: HomeUiStrings,
) {
    val context = LocalContext.current
    val softwareKeyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(proceedResult) {
        when (proceedResult) {
            is ProceedResult.Success -> {
                // keyboard blinks when hidden, similar issue: https://stackoverflow.com/q/76901241/8862499
                // the issue is somewhere in 'Color Input', probably in the internals of TextField()
                softwareKeyboardController?.hide()
            }
            is ProceedResult.InvalidSubmittedColor -> {
                Toast
                    .makeText(context, strings.invalidSubmittedColorMessage, Toast.LENGTH_SHORT)
                    .show()
                proceedResult.discard()
            }
            null -> doNothing()
        }
    }
}

@Composable
internal fun ScrollToTopOnNullProceedResultAsSideEffect(
    proceedResult: ProceedResult?,
    scrollState: ScrollState,
) {
    val cacheOfProceedResult = remember {
        DequeCache<ProceedResult?>(
            mutationListener = PruneOnSizeThreshold(cacheSizeThreshold = 2),
        )
    }
    LaunchedEffect(proceedResult) {
        val current = proceedResult
        // previous may be present but equal to 'null'
        if (cacheOfProceedResult.isNotEmpty()) {
            val previous = cacheOfProceedResult.last()
            val wasSuccessButBecameNull = (previous is ProceedResult.Success && current == null)
            if (wasSuccessButBecameNull && scrollState.value != 0) {
                scrollState.animateScrollTo(0)
            }
        }
        cacheOfProceedResult += proceedResult
    }
}