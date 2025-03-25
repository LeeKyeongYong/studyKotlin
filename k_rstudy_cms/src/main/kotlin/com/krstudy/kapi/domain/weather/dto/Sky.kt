package com.krstudy.kapi.domain.weather.dto

enum class Sky(val code: Int, val description: String) {
    CLEAR(1, "맑음"),
    PARTLY_CLOUDY(2, "구름조금"),
    MOSTLY_CLOUDY(3, "구름많음"),
    CLOUDY(4, "흐림");

    companion object {
        fun fromCode(code: Int): Sky = values().find { it.code == code }
            ?: throw IllegalArgumentException("잘못된 하늘상태 코드: $code")
    }
}