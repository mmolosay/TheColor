@file:Suppress("unused")

package io.github.mmolosay.thecolor.data.remote.model

import io.github.mmolosay.thecolor.data.remote.api.TheColorApiService
import com.squareup.moshi.Json

data class ColorSchemeResponse(
    @Json(name = "mode") val mode: TheColorApiService.SchemeMode,
    @Json(name = "count") val sampleCount: Int,
    @Json(name = "colors") val colors: List<ColorDetailsDto>,
    @Json(name = "seed") val seed: ColorDetailsDto,
)

