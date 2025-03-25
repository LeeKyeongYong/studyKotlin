package com.krstudy.kapi.domain.monitoring.controller

import com.krstudy.kapi.domain.monitoring.service.SystemMonitorService
import com.krstudy.kapi.global.https.ReqData
import jakarta.servlet.http.HttpServletResponse
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.MediaType
import java.nio.charset.StandardCharsets
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@Controller
@RequestMapping("/monitoring")
class MonitoringController(
    private val rq: ReqData,
    private val systemMonitorService: SystemMonitorService,
    private val messagingTemplate: SimpMessagingTemplate
) {
    @GetMapping
    fun showMonitoringDashboard(model: Model): String {
        val recentMetrics = systemMonitorService.getMetricsByDateRange(
            LocalDateTime.now().minusHours(1),
            LocalDateTime.now()
        ).body ?: listOf()
        model.addAttribute("metrics", recentMetrics)
        return "domain/monitoring/dashboard"
    }

    @GetMapping("/history")
    fun showMetricsHistory(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startDate: LocalDateTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endDate: LocalDateTime,
        response: HttpServletResponse
    ): String {
        response.contentType = MediaType.TEXT_HTML_VALUE
        response.characterEncoding = StandardCharsets.UTF_8.name()

        println("경용날짜: "+startDate+" : "+endDate)
        val metrics = systemMonitorService.getMetricsByDateRange(startDate, endDate).body ?: listOf()
        rq.setAttribute("metrics", metrics)
        return "domain/monitoring/history"
    }

    @Scheduled(fixedRate = 5000)
    fun sendMetricsUpdate() {
        val metrics = systemMonitorService.collectSystemMetrics()
        when (metrics) {
            is arrow.core.Either.Right -> {
                messagingTemplate.convertAndSend("/topic/metrics", metrics.value)
            }
            is arrow.core.Either.Left -> {
                println("Error collecting metrics: ${metrics.value}")
            }
        }
    }
}