package com.krstudy.kapi.domain.messages.service

import com.github.benmanes.caffeine.cache.Caffeine
import com.krstudy.kapi.domain.member.dto.MemberDto
import com.krstudy.kapi.domain.member.entity.Member
import com.krstudy.kapi.domain.member.service.MemberService
import com.krstudy.kapi.domain.messages.dto.MessageNotification
import com.krstudy.kapi.domain.messages.dto.MessageResponse
import com.krstudy.kapi.domain.messages.entity.Message
import com.krstudy.kapi.domain.messages.repository.MessageRepository
import com.krstudy.kapi.global.https.ReqData
import jakarta.transaction.Transactional
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.messaging.simp.SimpMessagingTemplate

@Service
class MessageService(
    private val messageRepository: MessageRepository,
    private val memberService: MemberService,
    private val reqData: ReqData // ReqData 주입
) {

    private val logger: Logger = LoggerFactory.getLogger(MessageService::class.java)

    private val messageCache = Caffeine.newBuilder()
        .maximumSize(10000)
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .build<Long, Message>()

    private val userCache = Caffeine.newBuilder()
        .maximumSize(300000)
        .expireAfterWrite(30, TimeUnit.MINUTES)
        .build<Long, Member>()

    suspend fun sendMessage(message: Message): Message {
        message.recipients.forEach { recipient ->
            val user = userCache.get(recipient.recipientId) {
                memberService.getMemberByNo(recipient.recipientId)
            }
            user?.let {
                recipient.recipientName = it.username ?: ""
            }
        }

        val savedMessage = messageRepository.save(message)
        messageCache.put(savedMessage.id, savedMessage)

        return savedMessage
    }

    suspend fun getUnreadMessagesCount(memberId: Long): Long {
        return messageRepository.countUnreadMessagesByRecipientId(memberId)
    }

    suspend fun searchUsers(searchUsername: String): List<MemberDto> {
        val members = memberService.searchMembersByUsername(searchUsername)
        return if (members.isNotEmpty()) {
            members.map { MemberDto.from(it) }
        } else {
            emptyList() // 검색 결과 없으면 빈 리스트 반환
        }
    }

    suspend fun getAllUsers(): List<MemberDto> {
        return memberService.getAllMembers().map { MemberDto.from(it) }
    }

    suspend fun getCurrentUser(): Member {
        return reqData.getMember()
            ?: throw IllegalStateException("현재 인증된 사용자를 찾을 수 없습니다.")
    }

    suspend fun getMessagesForUser(userId: Long, pageable: Pageable): Page<Message> {
        return messageRepository.findMessagesByUserId(userId, pageable)
    }

    suspend fun searchMessages(userId: Long, searchTerm: String, pageable: Pageable): Page<Message> {
        return messageRepository.searchMessages(userId, searchTerm, pageable)
    }

    // MessageService에서 유저가 보낸 메시지를 가져오는 메서드 추가
    suspend fun getSentMessages(userId: Long, pageable: Pageable): Page<Message> {
        return messageRepository.findBySenderId(userId, pageable)
    }

    // 검색 기능을 위한 메서드 추가
    suspend fun searchSentMessages(userId: Long, searchTerm: String, pageable: Pageable): Page<Message> {
        return messageRepository.searchSentMessages(userId, searchTerm, pageable)
    }

    suspend fun getMessageById(id: Long): Message {
        return messageRepository.findById(id).orElseThrow {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found")
        }
    }

    suspend fun getMemberById(id: Long): Member {
        return memberService.getMemberByNo(id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found")
    }

    @Transactional
    suspend fun markAsRead(messageId: Long, recipientId: Long) {
        val message = getMessageById(messageId)
        message.recipients.find { it.recipientId == recipientId }?.let { recipient ->
            recipient.readAt = LocalDateTime.now()
            messageRepository.save(message)
        }
    }

    suspend fun getMessagesForRecipientUserId(recipientUserId: String, pageable: Pageable): Page<Message> {
        return messageRepository.findMessagesByRecipientUserId(recipientUserId, pageable)
    }

    suspend fun searchMessagesForRecipientUserId(recipientUserId: Long, searchTerm: String, pageable: Pageable): Page<Message> {
        return messageRepository.searchMessages(recipientUserId, searchTerm, pageable)
    }

    @Transactional
    suspend fun markMessageAsReadAndGetUnreadCount(messageId: Long, recipientId: Long): Long {
        logger.info("Marking message $messageId as read for recipient $recipientId")
        messageRepository.markMessageAsRead(messageId, recipientId)

        val unreadCount = messageRepository.countUnreadMessagesByRecipientId(recipientId)
        logger.info("Unread count for recipient $recipientId: $unreadCount")

        return unreadCount
    }
    // 코루틴 래퍼 메소드 추가
    suspend fun markMessageAsReadAndGetUnreadCountSuspend(messageId: Long, recipientId: Long): Long {
        return withContext(Dispatchers.IO) {
            markMessageAsReadAndGetUnreadCount(messageId, recipientId)
        }
    }

    suspend fun getReceivedMessagesWithUnreadCount(recipientId: Long, pageable: Pageable): Pair<Page<Message>, Long> {
        val messages = messageRepository.findByRecipientsRecipientIdOrderByCreateDateDesc(recipientId, pageable)
        val unreadCount = messageRepository.countUnreadMessagesByRecipientId(recipientId)
        return Pair(messages, unreadCount)
    }

    suspend fun getUnreadMessages(userId: Long): List<MessageResponse> {
        return messageRepository.findUnreadMessagesByRecipientId(userId)
            .map { message ->
                MessageResponse.fromMessage(message, userId)
            }
    }

}