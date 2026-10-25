package io.github.mmolosay.thecolor.demo.presentation.feature.substitutes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
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
import io.github.mmolosay.thecolor.demo.domain.Sort

@Composable
fun Substitutes(
    feature: SubstitutesFeature,
    modifier: Modifier = Modifier,
) {
    val viewData by feature.viewData.collectAsStateWithLifecycle()
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.substitutes_title),
            style = MaterialTheme.typography.titleMedium,
        )
        when (val data = viewData) {
            SubstitutesViewData.NoAmount -> {
                Text(text = stringResource(R.string.substitutes_no_amount))
            }
            is SubstitutesViewData.Loading -> {
                SortChips(selected = data.sort, onSelect = { sort -> feature.sortBy(sort) })
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Text(text = stringResource(R.string.substitutes_loading))
                }
            }
            is SubstitutesViewData.Ready -> {
                SortChips(selected = data.sort, onSelect = { sort -> feature.sortBy(sort) })
                data.items.forEach { item ->
                    Item(item = item, onClick = { feature.select(item.facts) })
                }
            }
            is SubstitutesViewData.Failed -> {
                SortChips(selected = data.sort, onSelect = { sort -> feature.sortBy(sort) })
                Text(text = stringResource(R.string.substitutes_failed))
                TextButton(onClick = { feature.retry() }) {
                    Text(text = stringResource(R.string.substitutes_retry))
                }
            }
        }
    }
}

@Composable
private fun SortChips(
    selected: Sort,
    onSelect: (Sort) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Sort.entries.forEach { sort ->
            FilterChip(
                selected = (sort == selected),
                onClick = { onSelect(sort) },
                label = {
                    val label = when (sort) {
                        Sort.Calories -> R.string.substitutes_sort_calories
                        Sort.Protein -> R.string.substitutes_sort_protein
                    }
                    Text(text = stringResource(label))
                },
            )
        }
    }
}

@Composable
private fun Item(
    item: SubstitutesViewData.Item,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
    ) {
        Text(text = item.name)
        Text(
            text = stringResource(R.string.substitutes_item_figures, item.calories, item.protein),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
