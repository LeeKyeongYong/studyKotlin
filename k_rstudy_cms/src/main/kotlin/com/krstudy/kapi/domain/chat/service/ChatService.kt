package com.krstudy.kapi.domain.chat.service

import com.krstudy.kapi.domain.chat.entity.ChatMessage
import com.krstudy.kapi.domain.chat.entity.ChatRoom
import com.krstudy.kapi.domain.chat.repository.ChatMessageRepository
import com.krstudy.kapi.domain.chat.repository.ChatRoomRepository
import com.krstudy.kapi.domain.member.entity.Member
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ChatService(
    private val chatRoomRepository: ChatRoomRepository,
    private val chatMessageRepository: ChatMessageRepository
) {
    fun getChatRooms(): List<ChatRoom> = chatRoomRepository.findAll().filter { !it.isDeleted }

    fun getChatRoom(id: Long): ChatRoom {
        return chatRoomRepository.findById(id).orElseThrow { NoSuchElementException("Chat room not found with id: $id") }
    }

    fun createChatRoom(roomName: String, author: Member): ChatRoom {
        val chatRoom = ChatRoom(roomName = roomName, author = author)
        return chatRoomRepository.save(chatRoom)
    }

    fun deleteChatRoom(id: Long) {
        val chatRoom = getChatRoom(id)
        chatRoom.isDeleted = true
        chatRoomRepository.save(chatRoom)
    }

    fun restoreChatRoom(id: Long) {
        val chatRoom = getChatRoom(id)
        chatRoom.isDeleted = false
        chatRoomRepository.save(chatRoom)
    }

    fun getChatMessages(chatRoomId: Long): List<ChatMessage> {
        return chatMessageRepository.findByChatRoomId(chatRoomId)
    }

    fun writeChatMessage(chatRoomId: Long, writerName: String, content: String): ChatMessage {
        val chatMessage = ChatMessage(chatRoomId = chatRoomId, writerName = writerName, content = content)
        return chatMessageRepository.save(chatMessage)
    }

    fun getChatMessagesAfter(chatRoomId: Long, afterChatMessageId: Long): List<ChatMessage> {
        val chatMessages = chatMessageRepository.findByChatRoomId(chatRoomId)

        if (afterChatMessageId == -1L) {
            return chatMessages
        }

        return chatMessages.filter { it.id > afterChatMessageId }
    }
}