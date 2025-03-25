package com.krstudy.kapi.domain.messages.dto

data class RecipientDto(
    val recipientId: Long,
    val recipientName: String,
    val recipientUserId: String? // nullable로 변경
)