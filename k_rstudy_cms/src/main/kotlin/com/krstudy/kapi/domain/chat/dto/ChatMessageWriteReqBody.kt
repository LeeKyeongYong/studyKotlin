package com.krstudy.kapi.domain.chat.dto

import com.krstudy.kapi.domain.chat.entity.ChatMessage

data class ChatMessageWriteReqBody(
    val writerName: String,
    val content: String
){
    // 보조 생성자 추가
    constructor(chatMessage: ChatMessage) : this(
        writerName = chatMessage.writerName ?: "Unknown", // 기본값 제공
        content = chatMessage.content ?: "" // 기본값 제공
    )
}
