package com.krstudy.kapi.domain.popups.dto

import java.time.LocalDateTime

data class PopupHistoryDto(
    val id: Long,
    val popupId: Long,
    val action: String,
    val changeDetails: String?,  // nullable로 변경
    val editorId: String?,
    val createdAt: LocalDateTime
)