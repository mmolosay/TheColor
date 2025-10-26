package io.github.mmolosay.thecolor.presentation.impl.compose

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

/**
 * [clip] with fully [RoundedCornerShape].
 */
fun Modifier.clipFullyRounded() =
    this.clip(shape = RoundedCornerShape(percent = 100))