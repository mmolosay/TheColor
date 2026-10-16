package io.github.mmolosay.thecolor.demo.domain

import kotlinx.coroutines.flow.StateFlow

interface DemoSettings {
    val preferredUnit: StateFlow<AmountUnit?> // null until it's read
    val ouncesDecimalPlaces: StateFlow<Int?> // null until it's read
}
