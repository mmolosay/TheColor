package com.ordolabs.thecolor.util.ext

import android.text.Editable
import com.google.android.material.textfield.TextInputLayout

internal fun TextInputLayout.getText(): Editable? {
    return this.editText?.text
}