package com.krstudy.kapi.domain.weather.entity

import com.krstudy.kapi.domain.weather.dto.WeatherData
import com.krstudy.kapi.global.jpa.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "weather",
    indexes = [
        Index(name = "idx_weather_x_y_timestamp", columnList = "x,y,timestamp")
    ]
)
class Weather private constructor(
    @Column(nullable = false) val x: Int,
    @Column(nullable = false) val y: Int,
    @Column val hour: Int,
    @Embedded val data: WeatherData,
    @Column(nullable = false) val timestamp: LocalDateTime = LocalDateTime.now()
) : BaseEntity() {

    // getter 메서드 추가
    fun getTemperature() = data.temperature
    fun getSky() = data.sky
    fun getPrecipitation() = data.precipitation
    fun getDescription() = data.description
    fun getHumidity() = data.humidity
    fun getPrecipitationProbability() = data.precipitationProbability

    companion object {
        fun createWeather(
            x: Int,
            y: Int,
            hour: Int,
            temp: Double,
            sky: Int,
            pty: Int,
            wfKor: String,
            pop: Int,
            reh: Int
        ) = Weather(
            x = x,
            y = y,
            hour = hour,
            data = WeatherData(
                temperature = temp,
                sky = sky,
                precipitation = pty,
                description = wfKor,
                humidity = reh,
                precipitationProbability = pop
            )
        )
    }
}