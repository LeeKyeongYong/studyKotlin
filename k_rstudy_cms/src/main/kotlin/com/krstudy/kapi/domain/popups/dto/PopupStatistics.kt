package com.krstudy.kapi.domain.popups.dto

data class PopupStatistics(
    val viewCount: Long = 0,
    val clickCount: Long = 0,
    val ctr: Double = 0.0,
    val averageViewDuration: Double = 0.0
)