package com.krstudy.kapi.domain.chat.repository

import com.krstudy.kapi.domain.chat.entity.ChatMessage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ChatMessageRepository : JpaRepository<ChatMessage, Long> {
    fun findByChatRoomId(chatRoomId: Long): List<ChatMessage>
}