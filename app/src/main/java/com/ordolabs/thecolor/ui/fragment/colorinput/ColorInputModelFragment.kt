package com.ordolabs.thecolor.ui.fragment.colorinput

import com.github.michaelbull.result.Result

interface ColorInputModelFragment {

    fun isColorInputValid(): Boolean
    fun processColorInput(): Result<Unit, Boolean>
}