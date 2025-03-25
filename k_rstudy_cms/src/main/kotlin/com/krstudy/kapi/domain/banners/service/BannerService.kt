package com.krstudy.kapi.domain.banners.service

import com.krstudy.kapi.domain.banners.dto.BannerCreateRequest
import com.krstudy.kapi.domain.banners.dto.BannerResponse
import com.krstudy.kapi.domain.banners.entity.BannerEntity
import com.krstudy.kapi.domain.banners.repository.BannerRepository
import com.krstudy.kapi.domain.member.entity.Member  // Member 엔티티 import 추가
import com.krstudy.kapi.domain.member.repository.MemberRepository
import com.krstudy.kapi.domain.uploads.entity.FileEntity  // FileEntity import 추가
import com.krstudy.kapi.domain.uploads.exception.FileUploadException
import com.krstudy.kapi.domain.uploads.service.FileServiceImpl
import com.krstudy.kapi.global.exception.BannerCreationException
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class BannerService(
    private val bannerRepository: BannerRepository,
    private val fileService: FileServiceImpl,
    private val memberRepository: MemberRepository
) {
    @Transactional
    fun createBanner(request: BannerCreateRequest, imageFile: MultipartFile, userId: String): BannerResponse {
        try {
            validateBannerRequest(request)
            validateImageFile(imageFile)

            val creator = memberRepository.findByUserid(userId)
                ?: throw EntityNotFoundException("User not found")

            val bannerImage = fileService.uploadFiles(arrayOf(imageFile), userId).firstOrNull()
                ?: throw FileUploadException("Failed to upload banner image")

            val banner = createBannerEntity(request, bannerImage, creator)
            val savedBanner = bannerRepository.save(banner)
            return savedBanner.toResponse()
        } catch (e: Exception) {
            throw BannerCreationException("Failed to create banner: ${e.message}", e)
        }
    }

    private fun validateBannerRequest(request: BannerCreateRequest) {
        if (request.title.isBlank()) {
            throw IllegalArgumentException("Title cannot be empty")
        }
        if (request.description.isBlank()) {
            throw IllegalArgumentException("Description cannot be empty")
        }
        if (request.displayOrder < 1) {
            throw IllegalArgumentException("Display order must be greater than 0")
        }

        val startDateTime = parseDateSafely(request.startDate)
        val endDateTime = parseDateSafely(request.endDate)

        if (endDateTime.isBefore(startDateTime)) {
            throw IllegalArgumentException("End date must be after start date")
        }
    }

    private fun validateImageFile(file: MultipartFile) {
        if (file.isEmpty) {
            throw IllegalArgumentException("Image file cannot be empty")
        }
        // null 안전 연산자 수정
        if (file.contentType?.startsWith("image/") != true) {
            throw IllegalArgumentException("File must be an image")
        }
        if (file.size > 5_242_880) { // 5MB
            throw IllegalArgumentException("File size must be less than 5MB")
        }
    }

    private fun parseDateSafely(dateStr: String): LocalDateTime {
        return try {
            LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid date format: $dateStr")
        }
    }

    private fun createBannerEntity(
        request: BannerCreateRequest,
        bannerImage: FileEntity,
        creator: Member
    ): BannerEntity {
        return BannerEntity(
            title = request.title,
            description = request.description,
            linkUrl = request.linkUrl,
            displayOrder = request.displayOrder,
            bannerImage = bannerImage,
            creator = creator,
            startDate = parseDateSafely(request.startDate),
            endDate = parseDateSafely(request.endDate)
        )
    }

    @Transactional(readOnly = true)
    fun getActiveBanners(): List<BannerResponse> {
        return bannerRepository.findActiveBanners()
            .map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun getUserBanners(userId: String): List<BannerResponse> {
        return bannerRepository.findByCreatorId(userId)
            .map { it.toResponse() }
    }

    private fun BannerEntity.toResponse() = BannerResponse(
        id = id,
        title = title,
        description = description,
        linkUrl = linkUrl,
        displayOrder = displayOrder,
        status = status,
        imageUrl = "/api/v1/files/view/${bannerImage.id}", //이미지경로
        creatorName = creator.username,
        startDate = startDate,
        endDate = endDate,
        createDate = getCreateDate() ?: LocalDateTime.now()
    )
}