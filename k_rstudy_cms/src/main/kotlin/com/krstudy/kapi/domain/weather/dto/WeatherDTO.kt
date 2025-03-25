package com.krstudy.kapi.domain.weather.dto

import java.time.LocalDateTime

data class WeatherDTO(
    val temperature: Double,
    val sky: Int,
    val description: String,
    val hour: Int,
    val timestamp: LocalDateTime
)