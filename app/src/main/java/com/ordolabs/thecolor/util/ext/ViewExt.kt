package com.ordolabs.thecolor.util.ext

import android.text.Editable
import android.text.InputFilter
import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout

internal fun TextInputLayout.getText(): Editable? {
    return this.editText?.text
}

internal fun TextInputLayout.getTextString(): String? {
    return this.editText?.text?.toString()
}

internal fun EditText.addFilters(vararg filters: InputFilter) {
    val updated = this.filters.toMutableList().apply { addAll(filters) }
    this.filters = updated.toTypedArray()
}