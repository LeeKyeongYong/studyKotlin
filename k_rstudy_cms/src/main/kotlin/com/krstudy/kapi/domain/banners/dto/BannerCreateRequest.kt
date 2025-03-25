package com.krstudy.kapi.domain.banners.dto

data class BannerCreateRequest(
    val title: String,
    val description: String,
    val linkUrl: String?,
    val displayOrder: Int,
    val startDate: String,
    val endDate: String
)