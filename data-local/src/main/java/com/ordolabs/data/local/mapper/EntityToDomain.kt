package com.ordolabs.data.local.mapper

import com.ordolabs.data.local.model.ColorHistoryEntity
import com.ordolabs.domain.model.HistoryColor

internal fun ColorHistoryEntity.toDomain() = HistoryColor(
    a = "Fill me"
)