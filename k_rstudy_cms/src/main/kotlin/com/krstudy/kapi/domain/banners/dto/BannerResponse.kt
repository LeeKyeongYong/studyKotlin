package com.krstudy.kapi.domain.banners.dto

import com.krstudy.kapi.domain.banners.enums.BannerStatus
import java.time.LocalDateTime

data class BannerResponse(
    val id: Long,
    val title: String,
    val description: String,
    val linkUrl: String?,  // nullable
    val displayOrder: Int,
    val status: BannerStatus,
    val imageUrl: String,
    val creatorName: String?,  // nullable
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val createDate: LocalDateTime
)