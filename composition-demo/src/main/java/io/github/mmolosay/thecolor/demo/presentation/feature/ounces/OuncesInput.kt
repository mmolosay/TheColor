package io.github.mmolosay.thecolor.demo.presentation.feature.ounces

import android.widget.Toast
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
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
fun OuncesInput(
    feature: OuncesInputFeature,
    modifier: Modifier = Modifier,
) {
    val viewData by feature.viewData.collectAsStateWithLifecycle()
    TextField(
        feature = feature.textField,
        label = stringResource(R.string.ounces_input_label),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Done,
        ),
        onKeyboardAction = { feature.submit() }, // Done
        hasClearButton = true,
        supportingText = if (viewData.isPending) stringResource(R.string.input_pending) else null,
        outputTransformation = SeparatorTransformation(separator = viewData.decimalSeparator),
        modifier = modifier,
    )
    val answerToShow = viewData.answerToShow ?: return
    val context = LocalContext.current
    val rejection = stringResource(R.string.ounces_input_rejected)
    LaunchedEffect(answerToShow.submission) {
        if (answerToShow.answer == Submission.Answer.Rejected) {
            Toast.makeText(context, rejection, Toast.LENGTH_SHORT).show()
        }
        feature.answerShown(answerToShow.submission)
    }
}

// Shows the point the text holds as the separator the setting asks for. Both are one character, so
// the cursor maps one to one
private data class SeparatorTransformation(
    val separator: Char,
) : OutputTransformation {

    override fun TextFieldBuffer.transformOutput() {
        val point = this.asCharSequence().indexOf('.')
        if (point >= 0) this.replace(point, point + 1, separator.toString())
    }
}
