package io.github.mmolosay.thecolor.data.remote.api

import io.github.mmolosay.thecolor.data.remote.model.ColorDetailsDto
import io.github.mmolosay.thecolor.data.remote.model.ColorSchemeResponse
import com.squareup.moshi.Json
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * [https://www.thecolorapi.com/docs]
 *
 * All optional URI parameters are set to default values.
 */
@Suppress("unused")
interface TheColorApiService {

    @GET("id")
    suspend fun getColorDetails(
        @Query("hex") hex: String? = null,
        @Query("rgb") rgb: String? = null,
        @Query("hsl") hsl: String? = null,
        @Query("cmyk") cmyk: String? = null,
        @Query("format") format: ResponseFormat = ResponseFormat.JSON
    ): ColorDetailsDto

    @GET("scheme")
    suspend fun getColorScheme(
        @Query("hex") hex: String? = null,
        @Query("rgb") rgb: String? = null,
        @Query("hsl") hsl: String? = null,
        @Query("cmyk") cmyk: String? = null,
        @Query("format") format: ResponseFormat = ResponseFormat.JSON,
        @Query("mode") mode: SchemeMode = SchemeMode.ANALOGIC,
        @Query("count") swatchCount: Int = 5,
        @Query("w") svgSize: Int = 100,
        @Query("named") doPrintNames: Boolean = true,
    ) : ColorSchemeResponse

    enum class ResponseFormat {
        JSON,
        HTML,
        SVG;

        override fun toString(): String {
            return this.name.lowercase()
        }
    }

    enum class SchemeMode {
        @Json(name = "monochrome") MONOCHROME,
        @Json(name = "monochrome-dark") MONOCHROME_DARK,
        @Json(name = "monochrome-light") MONOCHROME_LIGHT,
        @Json(name = "analogic") ANALOGIC,
        @Json(name = "complement") COMPLEMENT,
        @Json(name = "analogic-complement") ANALOGIC_COMPLEMENT,
        @Json(name = "triad") TRIAD,
        @Json(name = "quad") QUAD;

        override fun toString(): String {
            return this.name.lowercase().replace(oldChar = '_', newChar = '-')
        }
    }
}