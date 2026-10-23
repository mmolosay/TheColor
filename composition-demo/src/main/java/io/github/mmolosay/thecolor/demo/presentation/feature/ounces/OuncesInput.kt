package io.github.mmolosay.thecolor.demo.presentation.feature.ounces

import android.widget.Toast
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.thecolor.demo.R
import io.github.mmolosay.thecolor.demo.presentation.feature.input.Submission
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextField

@Composable
fun OuncesInput(
    input: OuncesInputFeature,
    modifier: Modifier = Modifier,
) {
    val viewData by input.viewData.collectAsStateWithLifecycle()
    TextField(
        field = input.textField,
        label = stringResource(R.string.ounces_input_label),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(
            onDone = { input.submit() },
        ),
        hasClearButton = true,
        supportingText = if (viewData.isPending) stringResource(R.string.input_pending) else null,
        visualTransformation = SeparatorTransformation(separator = viewData.decimalSeparator),
        modifier = modifier,
    )
    val answerToShow = viewData.answerToShow ?: return
    val context = LocalContext.current
    val rejection = stringResource(R.string.ounces_input_rejected)
    LaunchedEffect(answerToShow.token) {
        if (answerToShow.answer == Submission.Answer.Rejected) {
            Toast.makeText(context, rejection, Toast.LENGTH_SHORT).show()
        }
        input.answerShown(answerToShow.token)
    }
}

// Shows the point the text holds as the separator the setting asks for. Both are one character, so
// every offset maps to itself
private data class SeparatorTransformation(
    val separator: Char,
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val shown = text.text.replace('.', this.separator)
        return TransformedText(AnnotatedString(shown), OffsetMapping.Identity)
    }
}
