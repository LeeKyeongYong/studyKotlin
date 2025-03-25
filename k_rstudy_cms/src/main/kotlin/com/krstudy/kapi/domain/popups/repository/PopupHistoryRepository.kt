package com.krstudy.kapi.domain.popups.repository

import com.krstudy.kapi.domain.popups.entity.PopupEntity
import com.krstudy.kapi.domain.popups.entity.PopupHistoryEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PopupHistoryRepository : JpaRepository<PopupHistoryEntity, Long> {
    fun findByPopupId(popupId: Long): List<PopupHistoryEntity>
    fun findByPopupOrderByCreateDateDesc(popup: PopupEntity): List<PopupHistoryEntity>
}