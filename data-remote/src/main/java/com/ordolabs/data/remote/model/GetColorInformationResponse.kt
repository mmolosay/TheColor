@file:Suppress("unused")

package com.ordolabs.data.remote.model

data class GetColorInformationResponse(
    val hex: HexModelResponse,
    val rgb: RgbModelResponse,
    val hsl: HslModelResponse,
    val hsv: HsvModelResponse,
    val xyz: XyzModelResponse,
    val cmyk: CmykModelResponse,
    val name: NameResponse,
    val image: ImageResponse,
    val contrast: ContrastResponse
)

data class HexModelResponse(
    val value: String,
    val clean: String
)

data class RgbModelResponse(
    val fraction: RgbModelFractionResponse,
    val r: Int,
    val g: Int,
    val b: Int,
    val value: String
)

data class RgbModelFractionResponse(
    val r: Float,
    val g: Float,
    val b: Float
)

data class HslModelResponse(
    val fraction: HslModelFractionResponse,
    val h: Int,
    val s: Int,
    val l: Int,
    val value: String
)

data class HslModelFractionResponse(
    val h: Float,
    val s: Float,
    val l: Float
)

data class HsvModelResponse(
    val fraction: HsvModelFractionResponse,
    val h: Int,
    val s: Int,
    val v: Int,
    val value: String
)

data class HsvModelFractionResponse(
    val h: Float,
    val s: Float,
    val v: Float
)

data class XyzModelResponse(
    val fraction: XyzModelFractionResponse,
    val x: Int,
    val y: Int,
    val z: Int,
    val value: String
)

data class XyzModelFractionResponse(
    val x: Float,
    val y: Float,
    val z: Float
)

data class CmykModelResponse(
    val fraction: CmykModelFractionResponse,
    val c: Int,
    val m: Int,
    val y: Int,
    val k: Int,
    val value: String
)

data class CmykModelFractionResponse(
    val c: Float,
    val m: Float,
    val y: Float,
    val k: Float
)

data class NameResponse(
    val value: String,
    val closest_named_hex: String,
    val exact_match_name: Boolean,
    val distance: Int
)

data class ImageResponse(
    val bare: String,
    val named: String
)

data class ContrastResponse(
    val value: String
)