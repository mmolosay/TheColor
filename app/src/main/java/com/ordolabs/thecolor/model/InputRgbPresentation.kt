package com.ordolabs.thecolor.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class InputRgbPresentation(
    val r: Int?,
    val g: Int?,
    val b: Int?
) : Parcelable