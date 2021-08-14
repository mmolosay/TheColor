package com.ordolabs.thecolor.viewmodel

import android.content.res.TypedArray
import androidx.core.content.res.getStringOrThrow
import kotlinx.coroutines.CoroutineExceptionHandler

class ColorInputViewModel : BaseViewModel() {

    override val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        // TODO: implement
    }

    fun getDropdownItems(typedArray: TypedArray): Array<String>? =
        kotlin.runCatching {
            val items = Array(typedArray.length()) {
                typedArray.getStringOrThrow(it)
            }
            typedArray.recycle()
            items
        }.getOrNull()
}