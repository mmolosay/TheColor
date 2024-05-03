package io.github.mmolosay.thecolor.data.remote.model

import com.squareup.moshi.Json

enum class ResponseFormatDto {
    @Json(name = "json") JSON,
    @Json(name = "html") HTML,
    @Json(name = "svg") SVG,
    ;

    override fun toString(): String {
        return this.name.lowercase()
    }
}