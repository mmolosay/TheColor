package io.github.mmolosay.thecolor.demo.presentation.feature.grams

import android.widget.Toast
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import io.github.mmolosay.thecolor.demo.R
import io.github.mmolosay.thecolor.demo.presentation.feature.input.Submission
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextField
import io.github.mmolosay.thecolor.presentation.common.composition.collectAsViewData

@Composable
fun GramsInput(
    input: GramsInputFeature,
    modifier: Modifier = Modifier,
) {
    val viewData by input.viewData.collectAsViewData()
    TextField(
        field = input.textField,
        label = stringResource(R.string.grams_input_label),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(
            onDone = { input.submit() },
        ),
        hasClearButton = true,
        modifier = modifier,
    )
    val answerToShow = viewData.answerToShow ?: return
    val context = LocalContext.current
    val rejection = stringResource(R.string.grams_input_rejected)
    LaunchedEffect(answerToShow.token) {
        if (answerToShow.answer == Submission.Answer.Rejected) {
            Toast.makeText(context, rejection, Toast.LENGTH_SHORT).show()
        }
        input.answerShown(answerToShow.token)
    }
}
