package com.krstudy.kapi.domain.weather.entity

import arrow.core.Either
import com.krstudy.kapi.domain.weather.dto.*
import java.time.LocalDateTime

data class WeatherInfo(
    val location: Location,
    val temperature: Temperature,
    val sky: Sky,
    val precipitation: Precipitation,
    val timestamp: LocalDateTime
) {
    // 스마트 생성자 패턴 적용
    companion object {
        fun create(
            x: Int,
            y: Int,
            temp: Double,
            skyCode: Int,
            precipitationCode: Int
        ): Either<WeatherError, WeatherInfo> {
            return Either.catch {
                WeatherInfo(
                    location = Location(x, y),
                    temperature = Temperature(temp),
                    sky = Sky.fromCode(skyCode),
                    precipitation = Precipitation.fromCode(precipitationCode),
                    timestamp = LocalDateTime.now()
                )
            }.mapLeft { WeatherError.InvalidData(it.message ?: "알 수 없는 오류") }
        }
    }
}