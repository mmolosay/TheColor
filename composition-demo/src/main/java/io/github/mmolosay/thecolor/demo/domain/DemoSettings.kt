package io.github.mmolosay.thecolor.demo.domain

import kotlinx.coroutines.flow.StateFlow

interface DemoSettings {
    val decimalSeparator: StateFlow<DecimalSeparator?> // null until it's read; can change any time
}
