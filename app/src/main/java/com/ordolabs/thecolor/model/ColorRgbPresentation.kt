package com.ordolabs.thecolor.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ColorRgbPresentation(
    val r: Int?,
    val g: Int?,
    val b: Int?
) : Parcelable