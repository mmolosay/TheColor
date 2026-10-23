package io.github.mmolosay.thecolor.demo.presentation.feature.recipe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.mmolosay.thecolor.demo.R
import io.github.mmolosay.thecolor.demo.presentation.feature.editor.AmountEditor
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.Nutrition
import io.github.mmolosay.thecolor.demo.presentation.feature.sheet.SubstituteSheet
import io.github.mmolosay.thecolor.demo.presentation.feature.substitutes.Substitutes
import io.github.mmolosay.thecolor.presentation.common.composition.collectAsViewData
import kotlinx.coroutines.launch

@Composable
fun RecipeScreen(
    viewModel: RecipeViewModel = hiltViewModel(),
) {
    val viewData by viewModel.viewData.collectAsViewData()
    val snackbarHostState = remember { SnackbarHostState() }
    // outlives each message's effect, which ends as soon as the message is acknowledged
    val snackbarScope = rememberCoroutineScope()
    Scaffold(
        modifier = Modifier.imePadding(), // so that a message shows above the keyboard
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .consumeWindowInsets(contentPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (viewData.isLoadingSavedAmount) {
                Text(text = stringResource(R.string.recipe_loading_saved_amount))
            }
            Presets(onApply = { preset -> viewModel.applyPreset(preset) })
            AmountEditor(editor = viewModel.editor)
            // only looks around the cards' Views, so they're the screen's, not features
            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Nutrition(
                    nutrition = viewModel.nutrition,
                    modifier = Modifier.padding(16.dp),
                )
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Substitutes(
                    substitutes = viewModel.substitutes,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
    SubstituteSheet(sheet = viewModel.sheet)
    val messageToShow = viewData.messageToShow ?: return
    val text = stringResource(R.string.recipe_saved, messageToShow.amount)
    // acknowledged as it starts showing, so that it's never shown twice, even if the screen is
    // recreated while it shows
    LaunchedEffect(messageToShow.token) {
        snackbarScope.launch { snackbarHostState.showSnackbar(text) }
        viewModel.messageShown(messageToShow.token)
    }
}

@Composable
private fun Presets(
    onApply: (RecipePreset) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        RecipePreset.entries.forEach { preset ->
            AssistChip(
                onClick = { onApply(preset) },
                label = {
                    val label = when (preset) {
                        RecipePreset.CupByProtein -> R.string.recipe_preset_cup_protein
                        RecipePreset.HalfCupByCalories -> R.string.recipe_preset_half_cup_calories
                    }
                    Text(text = stringResource(label))
                },
            )
        }
    }
}
