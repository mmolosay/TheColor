package com.ordolabs.thecolor.ui.fragment.colorinput

import com.github.michaelbull.result.Result

interface ColorInputModelFragment {

    fun validateColorInput()
    fun processColorInput(): Result<Unit, Boolean>
}