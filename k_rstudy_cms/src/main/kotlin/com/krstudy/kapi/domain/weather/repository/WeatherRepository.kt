package com.krstudy.kapi.domain.weather.repository

import com.krstudy.kapi.domain.weather.entity.Weather
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface WeatherRepository: JpaRepository<Weather, Long> {
    fun findByXAndY(x: Int, y: Int): Weather?
    @Query("SELECT w FROM Weather w WHERE w.x = :x AND w.y = :y ORDER BY w.timestamp DESC")
    fun findByXAndYOrderByTimestampDesc(x: Int, y: Int): List<Weather>
}