package com.krstudy.kapi.domain.weather.repository

import com.krstudy.kapi.domain.weather.dto.Location

interface LocationRepository {
    fun findAll(): List<Location>
}