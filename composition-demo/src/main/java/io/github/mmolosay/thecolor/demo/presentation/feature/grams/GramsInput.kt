package io.github.mmolosay.thecolor.demo.presentation.feature.grams

import android.widget.Toast
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.thecolor.demo.R
import io.github.mmolosay.thecolor.demo.presentation.feature.input.Submission
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextField

@Composable
fun GramsInput(
    input: GramsInputFeature,
    modifier: Modifier = Modifier,
) {
    val viewData by input.viewData.collectAsStateWithLifecycle()
    TextField(
        field = input.textField,
        label = stringResource(R.string.grams_input_label),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done,
        ),
        onKeyboardAction = { input.submit() }, // Done
        hasClearButton = true,
        supportingText = if (viewData.isPending) stringResource(R.string.input_pending) else null,
        outputTransformation = null,
        modifier = modifier,
    )
    val answerToShow = viewData.answerToShow ?: return
    val context = LocalContext.current
    val rejection = stringResource(R.string.grams_input_rejected)
    LaunchedEffect(answerToShow.submission) {
        if (answerToShow.answer == Submission.Answer.Rejected) {
            Toast.makeText(context, rejection, Toast.LENGTH_SHORT).show()
        }
        input.answerShown(answerToShow.submission)
    }
}
