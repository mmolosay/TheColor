package com.ordolabs.data.remote.api

import com.ordolabs.data.remote.model.GetColorInformationResponse
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
    suspend fun getColorInformation(
        @Query("hex") hex: String? = null,
        @Query("rgb") rgb: String? = null,
        @Query("hsl") hsl: String? = null,
        @Query("cmyk") cmyk: String? = null,
        @Query("format") format: ResponseFormat = ResponseFormat.JSON
    ): GetColorInformationResponse

    @GET("scheme")
    suspend fun getColorScheme(
        @Query("hex") hex: String? = null,
        @Query("rgb") rgb: String? = null,
        @Query("hsl") hsl: String? = null,
        @Query("cmyk") cmyk: String? = null,
        @Query("format") format: ResponseFormat = ResponseFormat.JSON,
        @Query("mode") type: SchemeType = SchemeType.ANALOGIC,
        @Query("count") count: Int = 5,
        @Query("w") svgSize: Int = 100,
        @Query("named") doPrintNames: Boolean = true,
    )

    enum class ResponseFormat {
        JSON,
        HTML,
        SVG;

        override fun toString(): String {
            return this.name.lowercase()
        }
    }

    enum class SchemeType {
        MONOCHROME,
        MONOCHROME_DARK,
        MONOCHROME_LIGHT,
        ANALOGIC,
        COMPLEMENT,
        ANALOGIC_COMPLEMENT,
        TRIAD,
        QUAD;

        override fun toString(): String {
            return this.name.lowercase().replace(oldChar = '_', newChar = '-')
        }
    }
}