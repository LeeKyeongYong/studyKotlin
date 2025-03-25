package com.krstudy.kapi.domain.popups.dto

data class ABTestResult(
    val popupA: PopupTestStats,
    val popupB: PopupTestStats,
    val winner: String?,
    val confidenceLevel: Double
)