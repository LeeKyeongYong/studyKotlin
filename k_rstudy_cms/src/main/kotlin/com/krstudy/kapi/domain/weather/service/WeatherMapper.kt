package com.krstudy.kapi.domain.weather.service

import com.krstudy.kapi.domain.weather.dto.WeatherDTO
import com.krstudy.kapi.domain.weather.entity.Weather
import org.springframework.stereotype.Component

@Component
class WeatherMapper {

    fun toDTO(entity: Weather): WeatherDTO {
        return WeatherDTO(
            temperature = entity.getTemperature(),
            sky = entity.getSky(),
            description = entity.getDescription(),
            hour = entity.hour,
            timestamp = entity.timestamp
        )
    }

    fun toEntity(dto: WeatherDTO, x: Int, y: Int): Weather {
        return Weather.createWeather(
            x = x,
            y = y,
            hour = dto.hour,
            temp = dto.temperature,
            sky = dto.sky,
            pty = 0, // 기본값 설정 필요
            wfKor = dto.description,
            pop = 0, // 기본값 설정 필요
            reh = 0  // 기본값 설정 필요
        )
    }

    fun List<Weather>.toDTOList(): List<WeatherDTO> = this.map { toDTO(it) }
}