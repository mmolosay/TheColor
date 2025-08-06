package io.github.mmolosay.thecolor.data.remote.model

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonClass
import com.squareup.moshi.ToJson

/**
 * @param value A String expected by server that represents a particular entry.
 */
@JsonClass(generateAdapter = false)
enum class SchemeModeDto(val value: String) {
    Monochrome("monochrome"),
    MonochromeDark("monochrome-dark"),
    MonochromeLight("monochrome-light"),
    Analogic("analogic"),
    Complement("complement"),
    AnalogicComplement("analogic-complement"),
    Triad("triad"),
    Quad("quad"),
    ;

    // Retrofit uses .toString() instead of Adapter below
    override fun toString(): String =
        this.value
}

class SchemeModeDtoAdapter {
    @ToJson
    fun toJson(mode: SchemeModeDto): String =
        mode.value

    @FromJson
    fun fromJson(json: String): SchemeModeDto? =
        SchemeModeDto.entries.firstOrNull { it.value == json }
}