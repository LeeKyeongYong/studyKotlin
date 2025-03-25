package com.krstudy.kapi.domain.monitoring.entity

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.krstudy.kapi.global.jpa.BaseEntity
import java.time.LocalDateTime
import jakarta.persistence.*

@Entity
@Table(name = "monitoring_data")
data class MonitoringData(

    val cpuUsage: Double,
    val memoryTotal: Long,
    val memoryUsed: Long,
    val memoryFree: Long,
    val timestamp: LocalDateTime = LocalDateTime.now()
) : BaseEntity() {
    companion object {
        fun create(
            cpuUsage: Double,
            memoryTotal: Long,
            memoryUsed: Long,
            memoryFree: Long
        ): Either<String, MonitoringData> =
            when {
                cpuUsage < 0 || cpuUsage > 100 -> "Invalid CPU usage".left()
                memoryTotal < 0 -> "Invalid total memory".left()
                memoryUsed < 0 -> "Invalid used memory".left()
                memoryFree < 0 -> "Invalid free memory".left()
                else -> MonitoringData(
                    cpuUsage = cpuUsage,
                    memoryTotal = memoryTotal,
                    memoryUsed = memoryUsed,
                    memoryFree = memoryFree
                ).right()
            }
    }
}