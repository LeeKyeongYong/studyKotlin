package com.krstudy.kapi.com.krstudy.kapi.domain.chat.dto

import java.time.LocalDateTime

data class ChatRoomDTO(
    val id: Long,
    val roomName: String?,
    val authorName: String?,
    val authorId: String?,
    val createDate: LocalDateTime?

)