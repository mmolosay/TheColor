package com.ordolabs.thecolor.util.ext

import android.text.Editable
import android.text.InputFilter
import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout

fun TextInputLayout.getText(): Editable? {
    return this.editText?.text
}

fun TextInputLayout.getTextString(): String? {
    return this.editText?.text?.toString()
}

fun EditText.addFilters(vararg filters: InputFilter) {
    val updated = this.filters.toMutableList().apply { addAll(filters) }
    this.filters = updated.toTypedArray()
}