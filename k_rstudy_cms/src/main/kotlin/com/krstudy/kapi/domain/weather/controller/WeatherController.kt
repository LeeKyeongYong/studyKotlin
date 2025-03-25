package com.krstudy.kapi.domain.weather.controller

import com.krstudy.kapi.domain.weather.dto.WeatherResponse
import com.krstudy.kapi.domain.weather.service.ImageService
import com.krstudy.kapi.domain.weather.service.WeatherService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class WeatherController(
    private val weatherService: WeatherService,
    private val imageService: ImageService
) {

    @GetMapping("/weather")
    fun getWeather(model: Model): String {
        val weather = weatherService.getRecentWeatherList(59, 125).firstOrNull()
        println("Fetched weather: $weather") // 디버깅용

        val weatherResponse = weather?.let {
            WeatherResponse(
                temperature = it.getTemperature(),
                sky = it.getSky(),
                pty = it.getPrecipitation(),
                description = it.getDescription()
            ).also {
                println("Weather response: $it") // 디버깅용
            }
        } ?: WeatherResponse(
            temperature = 0.0,
            sky = 0,
            pty = 0,
            description = "정보 없음"
        )

        model.addAttribute("weather", weatherResponse)
        return "domain/weather/weather"
    }

    @GetMapping("/weather2")
    fun getWeatherPage(
        @RequestParam x: Int,
        @RequestParam y: Int,
        model: Model
    ): String {
        val weatherResponse = weatherService.getWeatherForLocation(x, y)
        model.addAttribute("weather", weatherResponse)
        return "domain/weather/weather2"
    }

    @GetMapping("/weather/image/{x}/{y}")
    @ResponseBody
    fun getWeatherImage(@PathVariable x: Int, @PathVariable y: Int): ByteArray {
        val weather = weatherService.getWeather(x, y)
        return weather?.let { imageService.generateWeatherImage(it) } ?: ByteArray(0)
    }

    @GetMapping("/weather/api")
    @ResponseBody
    fun getWeatherData(
        @RequestParam x: Int,
        @RequestParam y: Int
    ): WeatherResponse = weatherService.getWeatherForLocation(x, y)

}