package com.krstudy.kapi.domain.popups.dto

data class PopupTestStats(
    val popupId: Long,
    val impressions: Long,
    val clicks: Long,
    val ctr: Double,
    val conversionRate: Double
)