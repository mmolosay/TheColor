@file:Suppress("unused")

package io.github.mmolosay.thecolor.data.remote.model

import com.squareup.moshi.Json

data class ColorSchemeDto(
    @Json(name = "mode") val mode: SchemeModeDto,
    @Json(name = "count") val sampleCount: Int,
    @Json(name = "colors") val swatches: List<ColorDetailsDto>,
    @Json(name = "seed") val seed: ColorDetailsDto,
)