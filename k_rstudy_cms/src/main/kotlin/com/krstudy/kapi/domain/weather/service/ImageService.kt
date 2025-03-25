package com.krstudy.kapi.domain.weather.service

import com.krstudy.kapi.domain.weather.entity.Weather
import org.springframework.stereotype.Service
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.ByteArrayOutputStream

@Service
class ImageService {
    fun generateWeatherImage(weather: Weather): ByteArray {
        val width = 200
        val height = 100
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val graphics: Graphics2D = image.createGraphics()

        graphics.color = Color.WHITE
        graphics.fillRect(0, 0, width, height)

        graphics.color = Color.BLACK
        graphics.font = Font("Arial", Font.PLAIN, 12)
        graphics.drawString("Temperature: ${weather.getTemperature()}", 10, 20)
        graphics.drawString("Sky: ${weather.getSky()}", 10, 40)
        graphics.drawString("Precipitation: ${weather.getPrecipitation()}", 10, 60)

        val outputStream = ByteArrayOutputStream()
        ImageIO.write(image, "png", outputStream)
        return outputStream.toByteArray()
    }
}