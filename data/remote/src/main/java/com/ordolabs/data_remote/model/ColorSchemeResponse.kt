@file:Suppress("unused")

package com.ordolabs.data_remote.model

import com.ordolabs.data_remote.api.TheColorApiService
import com.squareup.moshi.Json

data class ColorSchemeResponse(
    @Json(name = "mode") val mode: TheColorApiService.SchemeMode?,
    @Json(name = "count") val sampleCount: Int?,
    @Json(name = "colors") val colors: List<ColorDetailsResponse>?,
    @Json(name = "seed") val seed: ColorDetailsResponse?
)

