package com.krstudy.kapi.domain.popups.controller

import com.krstudy.kapi.domain.files.service.FileService
import com.krstudy.kapi.domain.popups.dto.*
import com.krstudy.kapi.domain.popups.enums.PopupStatus
import com.krstudy.kapi.domain.popups.service.PopupService
import com.krstudy.kapi.domain.popups.service.PopupStatisticsService  // 추가
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus

@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@RestController
@RequestMapping("/api/admin/popups", produces = [MediaType.APPLICATION_JSON_VALUE])
class PopupAdminController(
    private val popupService: PopupService,
    private val statisticsService: PopupStatisticsService,
    private val fileService: FileService
) {

    companion object {
        private val logger = LoggerFactory.getLogger(PopupAdminController::class.java)
    }

    @PostMapping("/{id}/activate")
    fun activatePopup(
        @PathVariable id: Long,
        @AuthenticationPrincipal user: UserDetails
    ): ResponseEntity<Void> {
        popupService.changePopupStatus(id, PopupStatus.ACTIVE, user.username)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/{id}/deactivate")
    fun deactivatePopup(
        @PathVariable id: Long,
        @AuthenticationPrincipal user: UserDetails
    ): ResponseEntity<Void> {
        popupService.changePopupStatus(id, PopupStatus.INACTIVE, user.username)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/{id}/history")
    fun getPopupHistory(@PathVariable id: Long): ResponseEntity<List<PopupHistoryDto>> {
        return ResponseEntity.ok(popupService.getPopupHistory(id))
    }

    @PostMapping("/bulk-update")
    fun bulkUpdate(
        @RequestBody updates: List<PopupBulkUpdateDto>,
        @AuthenticationPrincipal user: UserDetails
    ): ResponseEntity<List<PopupResponse>> {
        return ResponseEntity.ok(popupService.bulkUpdate(updates, user.username))
    }

    @PostMapping("/{id}/clone")
    fun clonePopup(
        @PathVariable id: Long,
        @AuthenticationPrincipal user: UserDetails  // 현재 사용자 정보 주입
    ): ResponseEntity<PopupResponse> {
        logger.info("Current user authorities: ${user.authorities}")  // 권한 로그 출력
        return try {
            val clonedPopup = popupService.clonePopup(id)
            ResponseEntity.ok(clonedPopup)
        } catch (e: Exception) {
            logger.error("Clone popup error", e)  // 에러 로깅
            ResponseEntity.badRequest().body(null)
        }
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    fun deletePopup(
        @PathVariable id: Long,
        @AuthenticationPrincipal user: UserDetails
    ): ResponseEntity<Map<String, Any>> {
        return try {
            // 팝업 삭제 (이미지 삭제 로직은 PopupService 내부에서 처리)
            popupService.deletePopup(id, user.username)

            ResponseEntity.ok(mapOf(
                "success" to true,
                "message" to "팝업이 성공적으로 삭제되었습니다."
            ))
        } catch (e: Exception) {
            logger.error("팝업 삭제 중 오류 발생", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf(
                "success" to false,
                "message" to "팝업 삭제 중 오류가 발생했습니다."
            ))
        }
    }

    @GetMapping("/{id}/statistics")
    fun getPopupStatistics(@PathVariable id: Long): ResponseEntity<Map<String, Any>> {
        return try {
            val statistics = statisticsService.getStatistics(id)  // 이제 올바른 메서드 호출
            ResponseEntity.ok(statistics)
        } catch (e: Exception) {
            logger.error("통계 조회 중 오류 발생", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "통계를 불러오는 중 오류가 발생했습니다."))
        }
    }

    @PostMapping("/{id}/record/{action}")
    fun recordAction(
        @PathVariable id: Long,
        @PathVariable action: String,
        @RequestParam(required = false) type: String?
    ): ResponseEntity<Void> {
        return try {
            when (action) {
                "view" -> statisticsService.recordView(id, type ?: "DESKTOP")
                "click" -> statisticsService.recordClick(id)
                "close" -> statisticsService.recordClose(id, type ?: "NORMAL")
            }
            ResponseEntity.ok().build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

}