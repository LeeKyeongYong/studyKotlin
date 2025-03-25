package com.krstudy.kapi.domain.popups.repository

import com.krstudy.kapi.domain.popups.entity.PopupStatisticsEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface PopupStatisticsRepository : JpaRepository<PopupStatisticsEntity, Long> {
    @Query("""
        SELECT ps FROM PopupStatisticsEntity ps 
        WHERE ps.popupId = :popupId 
        AND DATE(ps.createDate) = :date
    """)
    fun findHourlyStatsByPopupAndDate(
        @Param("popupId") popupId: Long,
        @Param("date") date: LocalDate
    ): List<PopupStatisticsEntity>

    @Query("""
        SELECT ps FROM PopupStatisticsEntity ps 
        WHERE ps.popupId = :popupId 
        GROUP BY ps.deviceType
    """)
    fun findDeviceStatsByPopup(popupId: Long): List<PopupStatisticsEntity>

    @Query("""
        SELECT ps FROM PopupStatisticsEntity ps 
        WHERE ps.popupId = :popupId
    """)
    fun findPopupStats(popupId: Long): PopupStatisticsEntity

    fun findByPopupId(popupId: Long): PopupStatisticsEntity?
}