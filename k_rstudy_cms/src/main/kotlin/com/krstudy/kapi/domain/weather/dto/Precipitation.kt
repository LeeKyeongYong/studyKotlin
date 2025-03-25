package com.krstudy.kapi.domain.weather.dto

enum class Precipitation(val code: Int, val description: String) {
    NONE(0, "없음"),
    RAIN(1, "비"),
    RAIN_SNOW(2, "비/눈"),
    SNOW_RAIN(3, "눈/비"),
    SNOW(4, "눈");

    companion object {
        fun fromCode(code: Int): Precipitation = values().find { it.code == code }
            ?: throw IllegalArgumentException("잘못된 강수 상태 코드: $code")
    }

}