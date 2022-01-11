package com.ordolabs.thecolor.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Presentation of HEX color, entered by user.
 * May contain non-valid color or nothing at all.
 *
 * @param value __signless__ HEX color `String`, e.g. "16A8C0".
 */
@Parcelize
data class InputHexPresentation(
    val value: String?
) : Parcelable