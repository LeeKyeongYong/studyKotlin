package com.krstudy.kapi.global.WebMvc

import com.krstudy.kapi.domain.popups.entity.PopupEntity
import com.krstudy.kapi.domain.popups.entity.PopupScheduleEntity
import com.krstudy.kapi.domain.popups.enums.PopupStatus
import com.krstudy.kapi.domain.popups.enums.RepeatType
import com.krstudy.kapi.domain.popups.repository.PopupRepository
import com.krstudy.kapi.domain.popups.repository.PopupScheduleRepository
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import java.time.LocalDateTime

@Configuration
class PopupSchedulingConfig(
    private val popupRepository: PopupRepository,
    private val popupScheduleRepository: PopupScheduleRepository
) {
    @Scheduled(fixedRate = 60000) // 1분마다 실행
    fun checkPopupSchedules() {
        val schedules = popupScheduleRepository.findAll()
        schedules.forEach { schedule ->
            when (schedule.repeatType) {
                RepeatType.WEEKLY -> handleWeeklyRepeat(schedule)
                RepeatType.MONTHLY -> handleMonthlyRepeat(schedule)
                RepeatType.DAILY -> handleDailyRepeat(schedule)
            }
        }
    }

    private fun handleWeeklyRepeat(schedule: PopupScheduleEntity) {
        val currentDayOfWeek = LocalDateTime.now().dayOfWeek.name.substring(0, 3)
        val scheduleDays = schedule.repeatDays?.split(",") ?: return

        val isScheduledDay = scheduleDays.any { it.trim() == currentDayOfWeek }
        if (isScheduledDay) {
            updatePopupStatus(schedule)
        }
    }

    private fun handleMonthlyRepeat(schedule: PopupScheduleEntity) {
        val currentDayOfMonth = LocalDateTime.now().dayOfMonth
        if (schedule.repeatMonthDay == currentDayOfMonth) {
            updatePopupStatus(schedule)
        }
    }

    private fun handleDailyRepeat(schedule: PopupScheduleEntity) {
        updatePopupStatus(schedule)
    }

    private fun updatePopupStatus(schedule: PopupScheduleEntity) {
        val now = LocalDateTime.now()
        val popup = schedule.popup ?: return

        val shouldBeActive = now.isAfter(schedule.startTime) &&
                now.isBefore(schedule.endTime) &&
                schedule.isActive

        if (shouldBeActive && popup.status != PopupStatus.ACTIVE) {
            popup.status = PopupStatus.ACTIVE
            popupRepository.save(popup)
        } else if (!shouldBeActive && popup.status == PopupStatus.ACTIVE) {
            popup.status = PopupStatus.INACTIVE
            popupRepository.save(popup)
        }
    }
}