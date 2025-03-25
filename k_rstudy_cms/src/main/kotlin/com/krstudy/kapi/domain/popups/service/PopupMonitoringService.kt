package com.krstudy.kapi.domain.popups.service

import com.krstudy.kapi.domain.popups.enums.AlertType
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory

@Service
class PopupMonitoringService {
    private val logger = LoggerFactory.getLogger(this::class.java)

    // 성능 모니터링
    fun trackPopupPerformance(popupId: Long) {
        // TODO: 성능 모니터링 구현
        logger.info("팝업 성능 모니터링: $popupId")
    }

    // 이상 징후 감지
    fun detectAnomalies() {
        // TODO: 이상 징후 감지 구현
        logger.info("이상 징후 감지 실행")
    }

    // 관리자 알림
    fun sendAlert(alertType: AlertType, message: String) {
        // TODO: 알림 전송 구현
        logger.warn("[${alertType.name}] $message")
    }
}