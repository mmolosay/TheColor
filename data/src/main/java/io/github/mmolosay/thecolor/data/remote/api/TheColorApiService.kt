package io.github.mmolosay.thecolor.data.remote.api

import io.github.mmolosay.thecolor.data.remote.model.ColorDetailsDto
import io.github.mmolosay.thecolor.data.remote.model.ColorSchemeDto
import io.github.mmolosay.thecolor.data.remote.model.ResponseFormatDto
import io.github.mmolosay.thecolor.data.remote.model.SchemeModeDto
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
        @Query("format") format: ResponseFormatDto = ResponseFormatDto.JSON,
    ): ColorDetailsDto

    @GET("scheme")
    suspend fun getColorScheme(
        @Query("hex") hex: String? = null,
        @Query("rgb") rgb: String? = null,
        @Query("hsl") hsl: String? = null,
        @Query("cmyk") cmyk: String? = null,
        @Query("format") format: ResponseFormatDto = ResponseFormatDto.JSON,
        @Query("mode") mode: SchemeModeDto = SchemeModeDto.Analogic,
        @Query("count") swatchCount: Int = 5,
        @Query("w") svgSize: Int = 100,
        @Query("named") doPrintNames: Boolean = true,
    ) : ColorSchemeDto
}