package com.krstudy.kapi.domain.messages.dto

data class MessageSaveRequest(
    val content: String,
    val title: String,
    val recipients: List<RecipientDto>
)