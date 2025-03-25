package com.krstudy.kapi.domain.popups.service

import com.krstudy.kapi.domain.popups.entity.PopupStatisticsEntity
import com.krstudy.kapi.domain.popups.repository.PopupRepository
import com.krstudy.kapi.domain.popups.repository.PopupStatisticsRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class PopupStatisticsService(
    private val statisticsRepository: PopupStatisticsRepository,
    private val popupRepository: PopupRepository
) {
    @Transactional
    fun getStatistics(popupId: Long): Map<String, Any> {
        val stats = statisticsRepository.findByPopupId(popupId)
            ?: createInitialStatistics(popupId)

        return mapOf(
            "totalViews" to stats.viewCount,
            "clickCount" to stats.clickCount,
            "ctr" to calculateCTR(stats.clickCount, stats.viewCount),
            "avgDuration" to stats.viewDuration,
            "deviceStats" to stats.deviceStats,
            "closeStats" to stats.closeTypeStats
        )
    }

    private fun calculateCTR(clicks: Long, views: Long): Double {
        return if (views > 0) (clicks.toDouble() / views) * 100 else 0.0
    }

    private fun createInitialStatistics(popupId: Long): PopupStatisticsEntity {
        val popup = popupRepository.findById(popupId).orElseThrow {
            EntityNotFoundException("팝업을 찾을 수 없습니다")
        }

        return PopupStatisticsEntity(
            popupId = popupId,
            deviceType = popup.deviceType,
            viewCount = 0,
            clickCount = 0,
            closeCount = 0,
            deviceStats = mutableMapOf(
                "DESKTOP" to 0L,
                "MOBILE" to 0L,
                "TABLET" to 0L
            ),
            closeTypeStats = mutableMapOf(
                "NORMAL" to 0L,
                "AUTO" to 0L,
                "TODAY" to 0L
            ),
            viewDuration = 0.0,
            hour = LocalDateTime.now().hour
        ).also {
            statisticsRepository.save(it)
        }
    }

    @Transactional
    fun recordView(popupId: Long, deviceType: String) {
        val stats = getOrCreateStats(popupId)
        stats.viewCount++
        stats.deviceStats[deviceType] = stats.deviceStats.getOrDefault(deviceType, 0L) + 1L
        statisticsRepository.save(stats)
    }

    @Transactional
    fun recordClick(popupId: Long) {
        val stats = getOrCreateStats(popupId)
        stats.clickCount++
        statisticsRepository.save(stats)
    }

    @Transactional
    fun recordClose(popupId: Long, closeType: String) {
        val stats = getOrCreateStats(popupId)
        stats.closeTypeStats[closeType] = stats.closeTypeStats.getOrDefault(closeType, 0L) + 1L
        stats.closeCount++
        statisticsRepository.save(stats)
    }

    private fun getOrCreateStats(popupId: Long): PopupStatisticsEntity {
        return statisticsRepository.findByPopupId(popupId) ?: createInitialStatistics(popupId)
    }
}