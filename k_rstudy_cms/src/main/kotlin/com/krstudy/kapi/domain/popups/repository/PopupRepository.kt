package com.krstudy.kapi.domain.popups.repository

import com.krstudy.kapi.domain.popups.entity.DeviceType
import com.krstudy.kapi.domain.popups.entity.PopupEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

/**
 * 팝업 레포지토리
 */
interface PopupRepository : JpaRepository<PopupEntity, Long> {

    /**
     * 현재 활성화된 팝업 중 우선순위가 높은 순으로 최대 3개 조회
     */
    @Query("""
        SELECT p FROM Popup p 
        WHERE p.status = 'ACTIVE'
        AND p.startDateTime <= :now 
        AND p.endDateTime >= :now
        AND (p.deviceType = :deviceType OR p.deviceType = 'ALL')
        ORDER BY p.priority DESC, p.createDate DESC
        LIMIT 3
    """)
    fun findActivePopups(
        @Param("now") now: LocalDateTime,
        @Param("deviceType") deviceType: DeviceType
    ): List<PopupEntity>

    /**
     * 특정 페이지에 표시될 팝업 조회
     */
    @Query("""
        SELECT p FROM Popup p 
        WHERE p.status = 'ACTIVE'
        AND p.startDateTime <= :now 
        AND p.endDateTime >= :now
        AND :page MEMBER OF p.displayPages
        ORDER BY p.priority DESC
    """)
    fun findPopupsByPage(
        @Param("page") page: String,
        @Param("now") now: LocalDateTime
    ): List<PopupEntity>
}