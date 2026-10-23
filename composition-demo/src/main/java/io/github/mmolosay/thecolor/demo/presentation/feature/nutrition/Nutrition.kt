package io.github.mmolosay.thecolor.demo.presentation.feature.nutrition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.thecolor.demo.R

@Composable
fun Nutrition(
    nutrition: NutritionFeature,
    modifier: Modifier = Modifier,
) {
    val viewData by nutrition.viewData.collectAsStateWithLifecycle()
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        when (val data = viewData) {
            NutritionViewData.NoAmount -> {
                Text(text = stringResource(R.string.nutrition_no_amount))
            }
            is NutritionViewData.Loading -> {
                Portion(portion = data.portion)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Text(text = stringResource(R.string.nutrition_loading))
                }
            }
            is NutritionViewData.Ready -> {
                Portion(portion = data.portion)
                Text(text = data.calories)
                Text(text = data.protein)
                val suggestion = data.suggestion
                if (suggestion != null) {
                    TextButton(onClick = { nutrition.selectSuggestion(suggestion.amount) }) {
                        Text(text = stringResource(R.string.nutrition_suggestion, suggestion.label))
                    }
                }
            }
            is NutritionViewData.Failed -> {
                Portion(portion = data.portion)
                Text(text = stringResource(R.string.nutrition_failed))
                TextButton(onClick = { nutrition.retry() }) {
                    Text(text = stringResource(R.string.nutrition_retry))
                }
            }
        }
    }
}

@Composable
private fun Portion(portion: NutritionViewData.Portion) {
    Text(
        text = stringResource(R.string.nutrition_portion, portion.ingredient, portion.amount),
        style = MaterialTheme.typography.titleMedium,
    )
}
