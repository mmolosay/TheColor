package io.github.mmolosay.thecolor.demo.presentation.feature.sheet

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.Nutrition
import io.github.mmolosay.thecolor.presentation.common.composition.collectAsViewData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubstituteSheet(
    sheet: SubstituteSheetFeature,
) {
    val viewData by sheet.viewData.collectAsViewData()
    val session = viewData.session ?: return
    // The control keeps a state of its own, such as being dismissed, so each session gets a control
    // of its own. A session opened right after the user closed one shows open, rather than the
    // control the user dismissed
    key(session) {
        ModalBottomSheet(
            onDismissRequest = { sheet.close(session) },
        ) {
            Nutrition(
                nutrition = sheet.nutrition,
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            )
        }
    }
}
