package com.krstudy.kapi.domain.messages.controller


import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.*
import org.springframework.http.MediaType // Add this import
import org.springframework.http.ResponseEntity
import com.krstudy.kapi.domain.messages.service.MessageService
import com.krstudy.kapi.domain.messages.dto.*
import com.krstudy.kapi.domain.member.dto.MemberDto
import com.krstudy.kapi.domain.messages.entity.Message
import com.krstudy.kapi.domain.messages.entity.MessageRecipient
import jakarta.servlet.http.HttpServletRequest
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/messages")
class ApiMessageController(
    private val messageService: MessageService,
    private val simpMessagingTemplate: SimpMessagingTemplate
) {
    private val logger = LoggerFactory.getLogger(ApiMessageController::class.java)
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun showMessageList(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = false) search: String?
        , httpServletRequest: HttpServletRequest
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Received request with Content-Type: ${httpServletRequest.contentType}")
        val pageable = PageRequest.of(page - 1, size, Sort.by("createDate").descending())
        val currentUser = messageService.getCurrentUser()

        // 로그인한 유저가 보낸 메시지만 가져오도록 수정
        val messagesPage = if (search.isNullOrBlank()) {
            messageService.getSentMessages(currentUser.id, pageable)
        } else {
            messageService.searchSentMessages(currentUser.id, search, pageable)
        }

        // 수신자 이름 포맷 변경
        val formattedMessages = messagesPage.content.map { message ->
            MessageResponse(
                id = message.id,
                content = message.content,
                title = message.title,
                senderId = message.senderId,
                senderName = currentUser.username, // 현재 사용자의 이름
                senderUserId = currentUser.userid, // 현재 사용자의 ID
                recipients = message.recipients.map {
                    RecipientDto(
                        recipientId = it.recipientId,
                        recipientName = "${it.recipientName} (${it.recipientUserId})",
                        recipientUserId = it.recipientUserId
                    )
                },
                sentAt = message.sentAt,
                readAt = message.recipients.find { it.recipientId != currentUser.id }?.readAt // 현재 사용자 기준으로 읽음 상태 처리
            )
        }

        val response = mapOf(
            "messages" to formattedMessages,
            "currentPage" to messagesPage.number + 1,
            "totalPages" to messagesPage.totalPages,
            "totalItems" to messagesPage.totalElements
        )

        return ResponseEntity.ok(response)
    }

    @GetMapping("/search-users", consumes = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun searchUsers(@RequestParam(required = false, defaultValue = "") searchUsername: String): ResponseEntity<List<MemberDto>> {

        // 현재 로그인한 사용자 정보 가져오기
        val currentUser = messageService.getCurrentUser()

        val users = if (searchUsername.isNullOrBlank()) {
            messageService.getAllUsers()  // 검색어 없으면 전체 유저 반환

        } else {
            messageService.searchUsers(searchUsername) // 검색어 있을 때 해당 유저 검색
        }

        // 로그인한 사용자를 목록에서 제외
        val filteredUsers = users.filter { it.userid != currentUser.userid }

        println("검색된 사용자 수: ${users.size}") // 검색된 사용자 수 출력
        println("검색된 사용자 목록: $users") // 전체 사용자 목록 출력

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(filteredUsers)
    }

    @PostMapping("/save")
    suspend fun saveMessage(@RequestBody request: MessageSaveRequest): ResponseEntity<Message> {

        val currentUser = messageService.getCurrentUser()
        val message = Message(
            content = request.content,
            senderId = currentUser.id,
            sentAt = LocalDateTime.now(),
            title = request.title
        )

        // 수신자 정보 설정
        request.recipients.forEach { recipientDto ->
            message.recipients.add(
                MessageRecipient(
                    message = message,
                    recipientId = recipientDto.recipientId, // 수정된 부분
                    recipientName = recipientDto.recipientName, // 수정된 부분
                    recipientUserId = recipientDto.recipientUserId // 추가된 부분
                )
            )
        }

        val savedMessage = messageService.sendMessage(message)

        // 각 수신자에게 알림 전송
        message.recipients.forEach { recipient ->
            val notification = MessageNotification(
                messageId = savedMessage.id,
                content = savedMessage.content,
                title = savedMessage.title,
                senderId = savedMessage.senderId
            )

            logger.info("Sending notification to userId: ${recipient.recipientId}, notification: $notification")
            // WebSocket을 통해 알림 전송
            simpMessagingTemplate.convertAndSend(
                "/topic/notifications/${recipient.recipientId}",
                notification
            )
        }


        return ResponseEntity.ok(savedMessage)
    }

    @GetMapping("/unread-count")
    fun getUnreadCount(): ResponseEntity<UnreadCountResponse> = runBlocking {
        val currentUser = messageService.getCurrentUser()
        val count = messageService.getUnreadMessagesCount(currentUser.id)
        ResponseEntity.ok(UnreadCountResponse(count))
    }

    @GetMapping("/received", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun showReceivedMessageList(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = false) search: String?
    ): ResponseEntity<Map<String, Any>> {
        val pageable = PageRequest.of(page - 1, size, Sort.by("createDate").descending())
        val currentUser = messageService.getCurrentUser()

        val messagesPage = if (search.isNullOrBlank()) {
            messageService.getMessagesForRecipientUserId(currentUser.userid, pageable)
        } else {
            messageService.searchMessagesForRecipientUserId(currentUser.id, search, pageable)
        }

        val formattedMessages = messagesPage.content.map { message ->
            MessageResponse.fromMessage(message, currentUser.id)
        }

        // 안읽은 메시지 수 가져오기
        val unreadCount = messageService.getUnreadMessagesCount(currentUser.id)

        val response = mapOf(
            "messages" to formattedMessages,
            "currentPage" to messagesPage.number + 1,
            "totalPages" to messagesPage.totalPages,
            "totalItems" to messagesPage.totalElements,
            "unreadCount" to unreadCount  // 안읽은 메시지 수 추가
        )

        return ResponseEntity.ok(response)
    }

    @PostMapping("/{messageId}/read")
    suspend fun markMessageAsRead(@PathVariable messageId: Long): ResponseEntity<UnreadCountResponse> {
        logger.info("Start: markMessageAsRead - messageId: $messageId")

        try {
            val currentUser = messageService.getCurrentUser()
            logger.info("Current user: ${currentUser.id}")

            // 새로운 suspend 함수 사용
            val unreadCount = messageService.markMessageAsReadAndGetUnreadCountSuspend(messageId, currentUser.id)
            logger.info("Unread count after marking as read: $unreadCount")

            return ResponseEntity.ok(UnreadCountResponse(unreadCount))
        } catch (e: Exception) {
            logger.error("Error in markMessageAsRead: ${e.message}", e)
            throw e
        }
    }
    @GetMapping("/unread")
    suspend fun getUnreadMessages(): List<MessageResponse> {
        val currentUser = messageService.getCurrentUser()
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        return messageService.getUnreadMessages(currentUser.id)
    }
}


