package com.krstudy.kapi.domain.monitoring.repository

import com.krstudy.kapi.domain.monitoring.entity.MonitoringData
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface MonitoringRepository : JpaRepository<MonitoringData, Long> {
    @Query("SELECT m FROM MonitoringData m WHERE m.timestamp BETWEEN :startDate AND :endDate")
    fun findByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): List<MonitoringData>

    fun findTop60ByOrderByTimestampDesc(): List<MonitoringData>
}