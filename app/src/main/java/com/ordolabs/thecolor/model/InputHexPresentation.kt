package com.ordolabs.thecolor.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class InputHexPresentation(
    val value: String?
) : Parcelable