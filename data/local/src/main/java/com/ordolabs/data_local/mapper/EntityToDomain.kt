package com.ordolabs.data_local.mapper

import com.ordolabs.data_local.model.ColorHistoryEntity
import com.ordolabs.domain.model.HistoryColor

fun ColorHistoryEntity.toDomain() = HistoryColor(
    a = "Fill me"
)