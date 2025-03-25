package com.krstudy.kapi.domain.weather.dto

sealed class WeatherError {
    data class InvalidData(val message: String) : WeatherError()
    data class NetworkError(val message: String) : WeatherError()
    data class DatabaseError(val message: String) : WeatherError()
    data class ApiError(val code: String, val message: String) : WeatherError()
    data class ParseError(val message: String) : WeatherError()

    // 오류 메시지를 사용자 친화적으로 변환
    fun toUserMessage(): String = when(this) {
        is InvalidData -> "잘못된 날씨 데이터입니다: $message"
        is NetworkError -> "네트워크 연결에 문제가 발생했습니다: $message"
        is DatabaseError -> "데이터베이스 오류가 발생했습니다: $message"
        is ApiError -> "날씨 정보를 가져오는데 실패했습니다 (오류 코드: $code): $message"
        is ParseError -> "날씨 데이터 처리 중 오류가 발생했습니다: $message"
    }

    // 로깅을 위한 상세 정보
    fun toLogMessage(): String = when(this) {
        is InvalidData -> "InvalidData: $message"
        is NetworkError -> "NetworkError: $message"
        is DatabaseError -> "DatabaseError: $message"
        is ApiError -> "ApiError[$code]: $message"
        is ParseError -> "ParseError: $message"
    }
}