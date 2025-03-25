package com.krstudy.kapi.domain.messages.dto

data class MessageNotification(
    val messageId: Long,
    val content: String,
    val title: String,
    val senderId: Long
)