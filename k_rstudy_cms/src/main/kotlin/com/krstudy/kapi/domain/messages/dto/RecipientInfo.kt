package com.krstudy.kapi.domain.messages.dto

import java.time.LocalDateTime

data class RecipientInfo(
    val id: Long,
    val name: String,
    val readAt: LocalDateTime?
)