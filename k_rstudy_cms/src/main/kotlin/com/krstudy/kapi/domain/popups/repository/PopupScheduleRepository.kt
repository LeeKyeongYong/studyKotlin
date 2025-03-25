package com.krstudy.kapi.domain.popups.repository

import com.krstudy.kapi.domain.popups.entity.PopupScheduleEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface PopupScheduleRepository : JpaRepository<PopupScheduleEntity, Long> {
    @Query("""
        SELECT ps FROM PopupScheduleEntity ps 
        WHERE ps.isActive = true 
        AND ps.startTime <= :now 
        AND ps.endTime >= :now
    """)
    fun findActiveSchedules(@Param("now") now: LocalDateTime): List<PopupScheduleEntity>
}