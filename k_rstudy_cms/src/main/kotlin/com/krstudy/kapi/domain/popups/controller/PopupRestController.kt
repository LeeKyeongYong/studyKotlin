package com.krstudy.kapi.domain.popups.controller

import com.krstudy.kapi.domain.popups.dto.PopupCloneSettingsDto
import com.krstudy.kapi.domain.popups.dto.PopupCreateRequest
import com.krstudy.kapi.domain.popups.dto.PopupResponse
import com.krstudy.kapi.domain.popups.entity.DeviceType
import com.krstudy.kapi.domain.popups.exception.PopupCreationException
import com.krstudy.kapi.domain.popups.service.PopupService
import jakarta.persistence.EntityNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

/**
 * 팝업 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/popups", produces = [MediaType.APPLICATION_JSON_VALUE])
class PopupRestController(
    private val popupService: PopupService
) {
    /**
     * 팝업 생성
     */
    @PostMapping
    fun createPopup(
        @RequestPart("popup") request: PopupCreateRequest,
        @RequestPart("image", required = false) image: MultipartFile?,
        @AuthenticationPrincipal userDetails: UserDetails?
    ): ResponseEntity<PopupResponse> {  // Any -> PopupResponse로 변경
        return try {
            if (userDetails == null) {
                ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(null)  // PopupResponse 타입에 맞춰 null 반환
            } else {
                val popup = popupService.createPopup(request, image, userDetails.username)
                ResponseEntity.ok(popup)
            }
        } catch (e: Exception) {
            println("팝업 생성 중 에러 발생: ${e.message}")
            e.printStackTrace()
            handleException(e)
        }
    }

    /**
     * 팝업 삭제 API
     */
    @DeleteMapping("/{id}")
    fun deletePopup(@PathVariable id: Long): ResponseEntity<Void> {
        popupService.deletePopup(id)
        return ResponseEntity.ok().build()
    }

    /**
     * 팝업 수정 API
     */
    @PutMapping("/{id}")
    fun updatePopup(
        @PathVariable id: Long,
        @RequestPart("popup") request: PopupCreateRequest,
        @RequestPart("image", required = false) image: MultipartFile?,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<PopupResponse> {
        val popup = popupService.updatePopup(id, request, image, userDetails.username)
        return ResponseEntity.ok(popup)
    }


    /**
     * 팝업 상세 조회 API
     */
    @GetMapping("/{id}")
    fun getPopup(@PathVariable id: Long): ResponseEntity<PopupResponse> {
        val popup = popupService.getPopup(id)
        return ResponseEntity.ok(popup)
    }


    /**
     * 활성화된 팝업 조회
     */
    @GetMapping
    fun getActivePopups(
        @RequestParam deviceType: String,
        @RequestParam(required = false) page: String?
    ): ResponseEntity<List<PopupResponse>> {
        val type = DeviceType.valueOf(deviceType.uppercase())
        return ResponseEntity.ok(popupService.getActivePopups(type, page))
    }

    /**
     * 팝업 조회수 증가
     */
    @PostMapping("/{id}/view")
    fun incrementViewCount(@PathVariable id: Long): ResponseEntity<Void> {
        popupService.incrementViewCount(id)
        return ResponseEntity.ok().build()
    }

    /**
     * 팝업 클릭수 증가
     */
    @PostMapping("/{id}/click")
    fun incrementClickCount(@PathVariable id: Long): ResponseEntity<Void> {
        popupService.incrementClickCount(id)
        return ResponseEntity.ok().build()
    }

    /**
     * 예외 처리 유틸리티 메서드
     */
    private fun handleException(e: Exception): ResponseEntity<PopupResponse> {  // 반환 타입 변경
        return when (e) {
            is PopupCreationException -> ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(null)  // PopupResponse 타입에 맞춰 null 반환

            is IllegalArgumentException -> ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(null)

            is EntityNotFoundException -> ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(null)

            else -> ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null)
        }
    }

}