package com.ordolabs.domain.usecase

import com.ordolabs.domain.model.ColorRgb
import javax.inject.Inject

class ValidateColorRgbUseCase @Inject constructor() {

    private val componentRange by lazy { 0..255 }

    operator fun invoke(color: ColorRgb?): Boolean {
        color ?: return false
        val components = listOf(color.r, color.b, color.g)
        return components.all { it in componentRange }
    }
}