package com.ordolabs.data.local.mapper

import com.ordolabs.data.local.model.ColorHistoryEntity
import com.ordolabs.domain.model.ColorHistory

internal fun ColorHistoryEntity.toDomain() = ColorHistory(
    a = "Fill me"
)