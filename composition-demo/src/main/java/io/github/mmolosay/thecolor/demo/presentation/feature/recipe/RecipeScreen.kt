package io.github.mmolosay.thecolor.demo.presentation.feature.recipe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.mmolosay.thecolor.demo.presentation.feature.editor.AmountEditor
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.Nutrition

@Composable
fun RecipeScreen(
    viewModel: RecipeViewModel = hiltViewModel(),
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AmountEditor(editor = viewModel.editor)
            // only looks around the card's View, so it's the screen's, not a feature
            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Nutrition(
                    nutrition = viewModel.nutrition,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}
