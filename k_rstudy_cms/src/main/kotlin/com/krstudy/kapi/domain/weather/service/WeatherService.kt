package com.krstudy.kapi.domain.weather.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.krstudy.kapi.domain.weather.dto.LocationDTO
import com.krstudy.kapi.domain.weather.dto.WeatherResponse
import com.krstudy.kapi.domain.weather.entity.Weather
import com.krstudy.kapi.domain.weather.repository.WeatherRepository
import org.jdom2.Document
import org.jdom2.Element
import org.jdom2.input.SAXBuilder
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.net.URL
import java.time.LocalDateTime

@Service
class WeatherService(private val weatherRepository: WeatherRepository) {

    private val rssFeed = "http://www.kma.go.kr/wid/queryDFS.jsp?gridx=%s&gridy=%s"

    private val logger = LoggerFactory.getLogger(WeatherService::class.java)

    fun updateWeatherData(x: Int, y: Int): Either<Throwable, Unit> {
        return try {
            logger.info("날씨 데이터 업데이트 시작: x=$x, y=$y")
            val weatherData = fetchWeatherData(x, y)
            weatherData.map {
                weatherRepository.save(it)
                logger.info("날씨 데이터 저장 완료")
            }
            Unit.right()
        } catch (e: Exception) {
            logger.error("날씨 데이터 업데이트 실패: ${e.message}")
            e.left()
        }
    }

    private fun fetchWeatherData(x: Int, y: Int): Either<Throwable, Weather> {
        return try {
            val url = String.format(rssFeed, x, y)
            val document: Document = SAXBuilder().build(URL(url))
            val root: Element = document.rootElement
            val body: Element = root.getChild("body")
            val data: Element = body.getChildren("data").first() // 현재 시점의 날씨 데이터

            val weather = Weather.createWeather(
                x = x,
                y = y,
                hour = data.getChildText("hour").toInt(),
                temp = data.getChildText("temp").toDouble(),
                sky = data.getChildText("sky").toInt(),
                pty = data.getChildText("pty").toInt(),
                wfKor = data.getChildText("wfKor"),
                pop = data.getChildText("pop").toInt(),
                reh = data.getChildText("reh").toInt()
            )

            weather.right()
        } catch (e: Exception) {
            println("날씨 데이터 가져오기 실패: ${e.message}")
            e.left()
        }
    }


    fun getWeather(x: Int, y: Int): Weather? {
        return weatherRepository.findByXAndY(x, y)
    }

    fun getWeatherForLocation(x: Int, y: Int): WeatherResponse {
        val weather = getWeather(x, y)
        return weather?.let {
            WeatherResponse(
                temperature = it.getTemperature(),
                sky = it.getSky(),
                pty = it.getPrecipitation(),
                description = it.getDescription()
            )
        } ?: WeatherResponse(
            temperature = 0.0,
            sky = 0,
            pty = 0,
            description = "정보 없음"
        )
    }

    fun getRecentWeatherList(x: Int, y: Int, limit: Int = 10): List<Weather> {
        logger.info("최근 날씨 데이터 조회 시작: x=$x, y=$y, limit=$limit")

        // 전체 데이터 수 확인
        val totalCount = weatherRepository.count()
        logger.info("전체 날씨 데이터 수: $totalCount")

        // 특정 좌표의 데이터 조회
        val weatherList = weatherRepository.findByXAndYOrderByTimestampDesc(x, y).take(limit)
        logger.info("조회된 데이터 수: ${weatherList.size}")

        // 첫 번째 데이터 상세 정보 출력
        weatherList.firstOrNull()?.let {
            logger.info("첫 번째 데이터 정보: x=${it.x}, y=${it.y}, timestamp=${it.timestamp}")
        }

        return weatherList
    }
}