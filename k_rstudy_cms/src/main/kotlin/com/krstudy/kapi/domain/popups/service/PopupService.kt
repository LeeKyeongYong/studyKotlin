package com.krstudy.kapi.domain.popups.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.krstudy.kapi.domain.member.entity.Member
import com.krstudy.kapi.domain.member.repository.MemberRepository
import com.krstudy.kapi.domain.popups.controller.PopupAdminController
import com.krstudy.kapi.domain.popups.dto.*
import com.krstudy.kapi.domain.popups.entity.*
import com.krstudy.kapi.domain.popups.enums.PopupStatus
import com.krstudy.kapi.domain.popups.exception.PopupCreationException
import com.krstudy.kapi.domain.popups.factory.PopupFactory
import com.krstudy.kapi.domain.popups.repository.*
import com.krstudy.kapi.domain.uploads.service.FileServiceImpl
import jakarta.persistence.EntityNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime


@Service
class PopupService(
    private val popupRepository: PopupRepository,
    private val fileService: FileServiceImpl,
    private val memberRepository: MemberRepository,
    private val popupFactory: PopupFactory,
    private val popupHistoryRepository: PopupHistoryRepository,
    private val popupTemplateRepository: PopupTemplateRepository,
    private val popupStatisticsRepository: PopupStatisticsRepository,
    private val popupScheduleRepository: PopupScheduleRepository,
    @Autowired private val objectMapper: ObjectMapper
) {

    companion object {
        private val logger = LoggerFactory.getLogger(PopupAdminController::class.java)
    }

    /**
     * 팝업 요청 검증
     */
    private fun validatePopupRequest(request: PopupCreateRequest) {
        require(request.title.isNotBlank()) { "제목은 필수입니다." }
        require(request.width in 100..2000) { "팝업 너비는 100px에서 2000px 사이여야 합니다." }
        require(request.height in 100..2000) { "팝업 높이는 100px에서 2000px 사이여야 합니다." }
        require(request.startDateTime < request.endDateTime) { "시작일시는 종료일시보다 이전이어야 합니다." }
    }

    /**
     * 이미지 파일 검증
     */
    private fun validateImageFile(file: MultipartFile) {
        require(file.contentType?.startsWith("image/") == true) { "이미지 파일만 업로드 가능합니다." }
        require(file.size <= 5 * 1024 * 1024) { "이미지 크기는 5MB를 초과할 수 없습니다." }
    }


    /**
     * 활성화된 팝업 조회
     */
    @Transactional(readOnly = true)
    fun getActivePopups(deviceType: DeviceType, page: String? = null): List<PopupResponse> {
        val now = LocalDateTime.now()
        val popups = if (page != null) {
            popupRepository.findPopupsByPage(page, now)
        } else {
            popupRepository.findActivePopups(now, deviceType)
        }
        return popups.map { PopupResponse.from(it) }
    }

    /**
     * 사용자 권한 기반 활성화된 팝업 조회
     */
    fun getActivePopups(deviceType: DeviceType, page: String?, user: Member?): List<PopupResponse> {
        val popups = popupRepository.findActivePopups(LocalDateTime.now(), deviceType)
        return popups.filter { popup ->
            popup.targetRoles.isEmpty() ||
                    user?.getAuthoritiesAsStringList()?.any { it in popup.targetRoles } == true
        }.map { PopupResponse.from(it) }
    }

    /**
     * 모든 팝업 조회
     */
    fun getAllPopups(): List<PopupResponse> {
        return popupRepository.findAll().map { PopupResponse.from(it) }
    }

    /**
     * 팝업 조회수 증가
     */
    @Transactional
    fun incrementViewCount(popupId: Long) {
        val popup = popupRepository.findById(popupId).orElseThrow {
            EntityNotFoundException("Popup not found")
        }
        popup.viewCount++
    }

    /**
     * 팝업 클릭수 증가
     */
    @Transactional
    fun incrementClickCount(popupId: Long) {
        val popup = popupRepository.findById(popupId).orElseThrow {
            EntityNotFoundException("팝업을 찾을 수 없습니다")
        }

        // 1. 팝업의 클릭 카운트 증가
        popup.clickCount++

        // 2. 통계 데이터 생성 또는 업데이트
        val stats = popupStatisticsRepository.findByPopupId(popupId)
            ?: createInitialStatistics(popup)

        // 3. 통계 데이터 업데이트
        stats.apply {
            clickCount++
            // CTR 계산을 위한 추가 데이터 업데이트
            if (viewCount > 0) {
                val ctr = (clickCount.toDouble() / viewCount) * 100
                // CTR 관련 데이터 업데이트
            }
        }

        // 4. 통계 데이터 저장
        popupStatisticsRepository.save(stats)
    }

    /**
     * 팝업 생성
     */
    @Transactional
    fun createPopup(
        request: PopupCreateRequest,
        image: MultipartFile?,
        userId: String
    ): PopupResponse {
        try {
            validatePopupRequest(request)
            image?.let { validateImageFile(it) }

            val creator = memberRepository.findByUserid(userId)
                ?: throw EntityNotFoundException("User not found")

            val popupImage = image?.let {
                fileService.uploadFiles(arrayOf(it), userId).firstOrNull()
            }

            val popup = popupFactory.createPopupEntity(request, popupImage, creator)
            val savedPopup = popupRepository.save(popup)
            return PopupResponse.from(savedPopup)
        } catch (e: Exception) {
            throw PopupCreationException("팝업 생성 실패: ${e.message}", e)
        }
    }

    /**
     * 팝업 삭제
     */
    @Transactional
    fun deletePopup(id: Long, userId: String? = null) {
        val popup = popupRepository.findById(id).orElseThrow {
            throw EntityNotFoundException("팝업을 찾을 수 없습니다: $id")
        }

        try {
            // userId가 있고 이미지가 있는 경우에만 이미지 삭제 시도
            if (userId != null && popup.image != null) {
                try {
                    fileService.deleteFile(popup.image!!.id, userId)
                } catch (e: Exception) {
                    // 이미지 삭제 실패 로깅
                    logger.warn("이미지 삭제 실패 - 팝업 ID: $id, 이미지 ID: ${popup.image?.id}", e)
                }
            }

            // 히스토리 기록
            userId?.let { uid ->
                val editor = memberRepository.findByUserid(uid)
                editor?.let {
                    savePopupHistory(popup, it, "DELETE",
                        mapOf("popupId" to id, "popupTitle" to popup.title))
                }
            }

            popupHistoryRepository.deleteById(id);

            // 팝업 삭제
            popupRepository.delete(popup)

        } catch (e: Exception) {
            throw RuntimeException("팝업 삭제 중 오류 발생: ${e.message}", e)
        }
    }

    /**
     * 팝업 상태 업데이트
     */
    @Transactional
    fun updatePopupStatus(id: Long, status: String): PopupResponse {
        val popup = popupRepository.findById(id).orElseThrow {
            EntityNotFoundException("팝업을 찾을 수 없습니다: $id")
        }
        popup.status = PopupStatus.valueOf(status.uppercase())
        return PopupResponse.from(popupRepository.save(popup))
    }

    /**
     * 팝업 수정
     */
    @Transactional
    fun updatePopup(
        id: Long,
        request: PopupCreateRequest,
        image: MultipartFile?,
        userId: String
    ): PopupResponse {
        try {
            validatePopupRequest(request)
            image?.let { validateImageFile(it) }

            val popup = popupRepository.findById(id).orElseThrow {
                EntityNotFoundException("팝업을 찾을 수 없습니다: $id")
            }

            val creator = memberRepository.findByUserid(userId)
                ?: throw EntityNotFoundException("사용자를 찾을 수 없습니다")

            val popupImage = image?.let {
                fileService.uploadFiles(arrayOf(it), userId).firstOrNull()
            } ?: popup.image

            // 팝업 정보 업데이트
            popup.apply {
                title = request.title
                content = request.content
                startDateTime = request.startDateTime
                endDateTime = request.endDateTime
                priority = request.priority
                width = request.width
                height = request.height
                positionX = request.positionX
                positionY = request.positionY
                this.image = popupImage
                linkUrl = request.linkUrl
                altText = request.altText
                target = request.target
                deviceType = request.deviceType
                cookieExpireDays = request.cookieExpireDays
                hideForToday = request.hideForToday
                hideForWeek = request.hideForWeek
                backgroundColor = request.backgroundColor
                borderStyle = request.borderStyle
                shadowEffect = request.shadowEffect
                animationType = request.animationType
                // Set 컬렉션 업데이트 수정
                displayPages = HashSet(request.displayPages)
                targetRoles = HashSet(request.targetRoles)
                maxDisplayCount = request.maxDisplayCount
            }

            return PopupResponse.from(popupRepository.save(popup))
        } catch (e: Exception) {
            throw PopupCreationException("팝업 수정 실패: ${e.message}", e)
        }
    }

    /**
     * 팝업 상세 조회
     */
    @Transactional(readOnly = true)
    fun getPopup(id: Long): PopupResponse {
        val popup = popupRepository.findById(id).orElseThrow {
            EntityNotFoundException("팝업을 찾을 수 없습니다: $id")
        }
        return PopupResponse.from(popup)
    }

    /**
     * 팝업 미리보기
     */
    fun previewPopup(id: Long): PopupPreviewResponse {
        val popup = popupRepository.findById(id).orElseThrow {
            EntityNotFoundException("Popup not found")
        }
        return PopupPreviewResponse.from(popup)
    }

    /**
     * 팝업 상태 변경
     */
    @Transactional
    fun changePopupStatus(id: Long, status: PopupStatus, userId: String) {
        val popup = popupRepository.findById(id).orElseThrow {
            EntityNotFoundException("Popup not found")
        }
        val editor = memberRepository.findByUserid(userId)
            ?: throw EntityNotFoundException("User not found")

        popup.status = status
        savePopupHistory(popup, editor, "STATUS_CHANGE",
            mapOf("oldStatus" to popup.status, "newStatus" to status))
    }



    /**
     * 템플릿 저장
     */
    @Transactional
    fun saveTemplate(request: TemplateCreateRequest, userId: String): TemplateResponse {
        val creator = memberRepository.findByUserid(userId)
            ?: throw EntityNotFoundException("User not found")

        val template = PopupTemplateEntity(
            name = request.name,
            content = request.content ?: "",  // 빈 문자열을 기본값으로 제공
            width = request.width,
            height = request.height,
            backgroundColor = request.backgroundColor,
            borderStyle = request.borderStyle,
            creator = creator,
            isDefault = request.isDefault
        )

        return popupTemplateRepository.save(template).toResponse()
    }

    /**
     * 통계 업데이트
     */
    @Transactional
    fun updateStatistics(id: Long, statsType: String, deviceType: String?) {
        val statistics = popupStatisticsRepository.findByPopupId(id)
            ?: throw EntityNotFoundException("Statistics not found")

        when (statsType) {
            "VIEW" -> {
                statistics.viewCount++
                deviceType?.let {
                    statistics.deviceStats.merge(it, 1L, Long::plus)
                }
            }
            "CLICK" -> statistics.clickCount++
            "CLOSE" -> {
                statistics.closeCount++
                statistics.closeTypeStats.merge(deviceType ?: "NORMAL", 1L, Long::plus)
            }
        }
    }



    private fun savePopupHistory(
        popup: PopupEntity,
        editor: Member,
        action: String,
        details: Map<String, Any>
    ) {
        val historyEntity = PopupHistoryEntity(
            popup = popup,
            editor = editor,
            action = action,
            changeDetails = objectMapper.writeValueAsString(details) // Map을 JSON 문자열로 변환
        )
        popupHistoryRepository.save(historyEntity)
    }

    /**
     * 팝업 이력 조회
     */
    @Transactional(readOnly = true)
    fun getPopupHistory(id: Long): List<PopupHistoryDto> {
        val popup = popupRepository.findById(id).orElseThrow {
            EntityNotFoundException("팝업을 찾을 수 없습니다: $id")
        }
        return popupHistoryRepository.findByPopupOrderByCreateDateDesc(popup)
            .map { history ->
                PopupHistoryDto(
                    id = history.id,
                    popupId = history.popup.id,
                    action = history.action,
                    changeDetails = history.changeDetails ?: "",  // null일 경우 빈 문자열 반환
                    editorId = history.editor.userid,
                    createdAt = history.getCreateDate() ?: LocalDateTime.now()
                )
            }
    }

    /**
     * 팝업 대량 업데이트
     */
    @Transactional
    fun bulkUpdate(updates: List<PopupBulkUpdateDto>, userId: String): List<PopupResponse> {
        val editor = memberRepository.findByUserid(userId)
            ?: throw EntityNotFoundException("사용자를 찾을 수 없습니다")

        return updates.map { update ->
            val popup = popupRepository.findById(update.id).orElseThrow {
                EntityNotFoundException("팝업을 찾을 수 없습니다: ${update.id}")
            }

            // 팝업 정보 업데이트
            update.status?.let {
                popup.status = it
                savePopupHistory(popup, editor, "STATUS_CHANGE",
                    mapOf("oldStatus" to popup.status, "newStatus" to it))
            }
            update.priority?.let {
                popup.priority = it
                savePopupHistory(popup, editor, "PRIORITY_CHANGE",
                    mapOf("oldPriority" to popup.priority, "newPriority" to it))
            }
            update.startDateTime?.let {
                val newDateTime = LocalDateTime.parse(it)
                savePopupHistory(popup, editor, "START_DATE_CHANGE",
                    mapOf("oldStartDate" to popup.startDateTime, "newStartDate" to newDateTime))
                popup.startDateTime = newDateTime
            }
            update.endDateTime?.let {
                val newDateTime = LocalDateTime.parse(it)
                savePopupHistory(popup, editor, "END_DATE_CHANGE",
                    mapOf("oldEndDate" to popup.endDateTime, "newEndDate" to newDateTime))
                popup.endDateTime = newDateTime
            }

            PopupResponse.from(popupRepository.save(popup))
        }
    }

    /**
     * 팝업 복제 (기본)
     */
    @Transactional
    fun clonePopup(id: Long): PopupResponse {
        val original = popupRepository.findById(id)
            .orElseThrow { EntityNotFoundException("팝업을 찾을 수 없습니다: $id") }

        // 기본 설정으로 복제를 위한 PopupCloneSettingsDto 생성
        val defaultSettings = PopupCloneSettingsDto(
            newTitle = "${original.title} (복사본)",
            inheritTargetRoles = true,
            inheritDisplayPages = true,
            inheritSchedule = true
        )

        // 기존의 상세 복제 메서드 호출
        return clonePopup(id, defaultSettings, original.creator.userid)
    }

    /**
     * 설정을 상속받은 팝업 복제 (상세)
     */
    @Transactional
    fun clonePopup(
        id: Long,
        settings: PopupCloneSettingsDto,
        userId: String
    ): PopupResponse {
        // 기존 코드 유지
        val originalPopup = popupRepository.findById(id).orElseThrow {
            EntityNotFoundException("팝업을 찾을 수 없습니다")
        }
        val creator = memberRepository.findByUserid(userId)
            ?: throw EntityNotFoundException("사용자를 찾을 수 없습니다")

        val clonedPopup = originalPopup.copy(
            title = settings.newTitle ?: "${originalPopup.title} (복사본)",
            status = PopupStatus.INACTIVE,
            creator = creator
        ).apply {
            if (!settings.inheritTargetRoles) {
                targetRoles = emptySet()
            }
            if (!settings.inheritDisplayPages) {
                displayPages = emptySet()
            }
            if (!settings.inheritSchedule) {
                startDateTime = LocalDateTime.now()
                endDateTime = LocalDateTime.now().plusDays(7)
            }
        }

        val savedPopup = popupRepository.save(clonedPopup)
        savePopupHistory(savedPopup, creator, "CLONE_WITH_SETTINGS",
            mapOf(
                "originalId" to id,
                "inheritTargetRoles" to settings.inheritTargetRoles,
                "inheritDisplayPages" to settings.inheritDisplayPages,
                "inheritSchedule" to settings.inheritSchedule
            )
        )

        return savedPopup.toResponse()
    }

    /**
     * 기본 템플릿 목록 조회
     */
    @Transactional(readOnly = true)
    fun getDefaultTemplates(): List<TemplateResponse> {
        return popupTemplateRepository.findByIsDefaultTrue()
            .map { it.toResponse() }
    }

    /**
     * 사용자 정의 템플릿 목록 조회
     */
    @Transactional(readOnly = true)
    fun getCustomTemplates(): List<TemplateResponse> {
        return popupTemplateRepository.findByIsDefaultFalse()
            .map { it.toResponse() }
    }

    /**
     * 템플릿 상세 조회
     */
    @Transactional(readOnly = true)
    fun getTemplate(id: Long): TemplateResponse {
        val template = popupTemplateRepository.findById(id).orElseThrow {
            EntityNotFoundException("템플릿을 찾을 수 없습니다: $id")
        }
        return template.toResponse()
    }

    /**
     * 템플릿 수정
     */
    @Transactional
    fun updateTemplate(
        id: Long,
        request: TemplateCreateRequest,
        userId: String
    ): TemplateResponse {
        val template = popupTemplateRepository.findById(id).orElseThrow {
            EntityNotFoundException("템플릿을 찾을 수 없습니다: $id")
        }

        // 템플릿 수정 권한 확인
        if (!template.isDefault && template.creator.userid != userId) {
            throw AccessDeniedException("템플릿을 수정할 권한이 없습니다.")
        }

        template.update(
            name = request.name,
            content = request.content,
            width = request.width,
            height = request.height,
            backgroundColor = request.backgroundColor,
            borderStyle = request.borderStyle,
            isDefault = request.isDefault
        )

        return popupTemplateRepository.save(template).toResponse()
    }

    /**
     * 템플릿 삭제
     */
    @Transactional
    fun deleteTemplate(id: Long) {
        val template = popupTemplateRepository.findById(id).orElseThrow {
            EntityNotFoundException("템플릿을 찾을 수 없습니다: $id")
        }

        // 기본 템플릿은 삭제 불가
        if (template.isDefault) {
            throw IllegalStateException("기본 템플릿은 삭제할 수 없습니다.")
        }

        popupTemplateRepository.delete(template)
    }

    /**
     * 템플릿 미리보기
     */
    @Transactional(readOnly = true)
    fun previewTemplate(id: Long): TemplateResponse {
        val template = popupTemplateRepository.findById(id) .orElseThrow { EntityNotFoundException("Template not found") }
        return TemplateResponse.from(template)
    }

    @Transactional
    fun getPopupStatistics(id: Long): Map<String, Any> {
        val popup = popupRepository.findById(id).orElseThrow {
            EntityNotFoundException("팝업을 찾을 수 없습니다: $id")
        }

        // 통계 데이터 조회 또는 생성
        val stats = popupStatisticsRepository.findByPopupId(id)
            ?: createInitialStatistics(popup)

        // 실제 데이터를 기반으로 통계 계산
        return mapOf(
            "totalViews" to stats.viewCount,
            "ctr" to calculateCTR(stats.clickCount, stats.viewCount),
            "avgDuration" to (stats.viewDuration),
            "deviceStats" to stats.deviceStats,
            "closeTypeStats" to stats.closeTypeStats
        )
    }

    private fun calculateCTR(clicks: Long, views: Long): Double {
        return if (views > 0) {
            (clicks.toDouble() / views) * 100
        } else 0.0
    }


    @Transactional
    protected fun createInitialStatistics(popup: PopupEntity): PopupStatisticsEntity {
        val stats = PopupStatisticsEntity(
            popupId = popup.id,
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
        )
        return popupStatisticsRepository.save(stats)
    }


    @Transactional
    protected fun updatePopupStatistics(id: Long, type: String, deviceType: String? = null) {
        val stats = popupStatisticsRepository.findByPopupId(id)
            ?: throw EntityNotFoundException("통계 정보를 찾을 수 없습니다.")

        when (type) {
            "VIEW" -> {
                stats.viewCount++
                deviceType?.let {
                    stats.deviceStats.merge(it, 1L, Long::plus)
                }
            }
            "CLICK" -> stats.clickCount++
            "CLOSE" -> {
                stats.closeCount++
                val closeType = deviceType ?: "NORMAL"
                stats.closeTypeStats.merge(closeType, 1L, Long::plus)
            }
        }

        popupStatisticsRepository.save(stats)
    }

    @Transactional
    fun incrementViewCount(popupId: Long, deviceType: String? = null) {
        val popup = popupRepository.findById(popupId).orElseThrow {
            EntityNotFoundException("팝업을 찾을 수 없습니다")
        }
        popup.viewCount++
        updatePopupStatistics(popupId, "VIEW", deviceType)
    }



    @Transactional
    fun recordPopupClose(popupId: Long, closeType: String) {
        updatePopupStatistics(popupId, "CLOSE", closeType)
    }

}