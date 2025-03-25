package com.krstudy.kapi.domain.monitoring.service

import arrow.core.Either
import com.krstudy.kapi.domain.monitoring.entity.MonitoringData
import com.krstudy.kapi.domain.monitoring.repository.MonitoringRepository
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.messaging.simp.SimpMessagingTemplate
import java.time.LocalDateTime
import oshi.SystemInfo

@Service
class SystemMonitorService(
    private val monitoringRepository: MonitoringRepository,
    private val messagingTemplate: SimpMessagingTemplate
) {
    private val systemInfo = SystemInfo()
    private val hardware = systemInfo.hardware

    fun collectSystemMetrics(): Either<String, MonitoringData> {
        return try {
            val processor = hardware.processor
            val memory = hardware.memory

            val data = MonitoringData.create(
                cpuUsage = processor.getSystemCpuLoad(1000) * 100,
                memoryTotal = memory.total,
                memoryUsed = memory.total - memory.available,
                memoryFree = memory.available
            )

            when (data) {
                is Either.Right -> {
                    val savedData = monitoringRepository.save(data.value)
                    messagingTemplate.convertAndSend("/topic/metrics", savedData)
                    Either.Right(savedData)
                }
                is Either.Left -> data
            }
        } catch (e: Exception) {
            Either.Left("Error collecting metrics: ${e.message}")
        }
    }

    fun getMetricsByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): ResponseEntity<List<MonitoringData>> {
        val metrics = monitoringRepository.findByDateRange(startDate, endDate)
        return ResponseEntity.ok().body(metrics)
    }
}