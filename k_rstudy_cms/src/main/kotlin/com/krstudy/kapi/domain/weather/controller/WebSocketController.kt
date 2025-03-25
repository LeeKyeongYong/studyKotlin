package com.krstudy.kapi.domain.weather.controller

import com.krstudy.kapi.domain.weather.dto.WeatherLocation
import com.krstudy.kapi.domain.weather.dto.WeatherResponse
import com.krstudy.kapi.domain.weather.service.WeatherService
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Controller

@Controller
class WeatherWebSocketController(
    private val weatherService: WeatherService
) {
    private val logger = LoggerFactory.getLogger(WeatherWebSocketController::class.java)

    @MessageMapping("/weather")
    @SendTo("/topic/weather")
    fun handleWeatherUpdate(location: WeatherLocation): WeatherResponse {
        logger.info("WebSocket 요청 받음: x=${location.x}, y=${location.y}")

        // 최신 데이터 가져오기
        val latestWeather = weatherService.getRecentWeatherList(location.x, location.y, 1).firstOrNull()

        return latestWeather?.let {
            logger.info("날씨 데이터 찾음: temp=${it.getTemperature()}, sky=${it.getSky()}")
            WeatherResponse(
                temperature = it.getTemperature(),
                sky = it.getSky(),
                pty = it.getPrecipitation(),
                description = it.getDescription()
            )
        } ?: run {
            logger.warn("날씨 데이터를 찾을 수 없음")
            WeatherResponse(
                temperature = 0.0,
                sky = 0,
                pty = 0,
                description = "정보 없음"
            )
        }
    }
}