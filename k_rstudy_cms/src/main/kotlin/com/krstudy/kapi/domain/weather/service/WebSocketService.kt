package com.krstudy.kapi.domain.weather.service

import com.krstudy.kapi.domain.weather.entity.Weather
import org.springframework.stereotype.Service
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import java.util.concurrent.CopyOnWriteArrayList

@Service
class WebSocketService {

    private val sessions = CopyOnWriteArrayList<WebSocketSession>()

    fun registerSession(session: WebSocketSession) {
        sessions.add(session)
    }

    fun removeSession(session: WebSocketSession) {
        sessions.remove(session)
    }

    fun sendWeatherUpdate(weather: Weather) {
        val message = TextMessage("Weather update: ${weather.getTemperature()}°C, Sky: ${weather.getSky()}")
        sessions.forEach { session ->
            session.sendMessage(message)
        }
    }
}