package io.github.mmolosay.thecolor.data.local.mapper

import io.github.mmolosay.thecolor.data.local.model.ColorHistoryEntity
import io.github.mmolosay.thecolor.domain.model.HistoryColor

fun ColorHistoryEntity.toDomain() = HistoryColor(
    a = "Fill me"
)