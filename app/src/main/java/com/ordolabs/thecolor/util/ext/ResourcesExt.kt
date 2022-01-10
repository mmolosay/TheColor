package com.ordolabs.thecolor.util.ext

import android.content.res.Resources
import com.ordolabs.thecolor.R

fun Resources.getStringYesOrNo(yes: Boolean): String {
    return if (yes) {
        getString(R.string.generic_yes)
    } else {
        getString(R.string.generic_no)
    }
}

fun Resources.getStringOrNull(res: Int): String? =
    Result.runCatching {
        getString(res)
    }.getOrNull()